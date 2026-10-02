package no.loopacademy.services;

import java.util.List;

import org.hibernate.Hibernate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import no.loopacademy.exceptions.ActionNotFoundException;
import no.loopacademy.exceptions.ForbiddenException;
import no.loopacademy.exceptions.OverloadedException;
import no.loopacademy.exceptions.ResourceConflictException;
import no.loopacademy.exceptions.SurvivorNotFoundException;
import no.loopacademy.exceptions.UserAlreadyHasSurvivorException;
import no.loopacademy.models.actions.Action;
import no.loopacademy.models.actions.ActionResult;
import no.loopacademy.models.audit.AuditActionType;
import no.loopacademy.models.items.Item;
import no.loopacademy.models.skills.Skill;
import no.loopacademy.models.survivors.Survivor;
import no.loopacademy.models.survivors.SurvivorType;
import no.loopacademy.models.userprofile.UserProfile;
import no.loopacademy.repositories.ActionRepository;
import no.loopacademy.repositories.SurvivorRepository;
import no.loopacademy.repositories.UserProfileRepository;

@Service
public class SurvivorService {

    private final SurvivorRepository survivorRepository;
    private final ActionRepository actionRepository;
    private final UserProfileRepository userProfileRepository;
    private final AuditEntryService auditEntryService;

    public SurvivorService(
        SurvivorRepository survivorRepository,
        ActionRepository actionRepository,
        UserProfileRepository userProfileRepository,
        AuditEntryService auditEntryService
    ) {
        this.survivorRepository = survivorRepository;
        this.actionRepository = actionRepository;
        this.userProfileRepository = userProfileRepository;
        this.auditEntryService = auditEntryService;
    }

    // The controller looks up the user (from the JWT) and passes it in.
    // The user comes from an earlier transaction, so Hibernate no longer tracks it:
    // it has to be saved explicitly for the new survivor_id to be written.
    @Transactional
    public Survivor create(UserProfile user, String name, SurvivorType type) {
        if (survivorRepository.existsByName(name)) {
            throw new ResourceConflictException("A survivor with this name already exists");
        }
        if (user.hasSurvivor()) {
                throw new UserAlreadyHasSurvivorException("This user already has a survivor");
            } //409 error
        try {
            // saveAndFlush so a unique-constraint violation surfaces here, not at commit
            

            Survivor survivor = survivorRepository.saveAndFlush(new Survivor(name, type));
            user.setSurvivor(survivor);
            userProfileRepository.save(user);
            auditEntryService.create(user, AuditActionType.SURVIVOR_CREATED, "Survivor", survivor.getId(),
                "Created " + type + " survivor " + name);
            return survivor;
        } catch (DataIntegrityViolationException e) {
            // Two simultaneous first requests: both passed the check above, the database stopped the second
            throw new ResourceConflictException("A survivor with this name already exists");
        }
    }

    @Transactional(readOnly = true)
    public List<Survivor> findAll() {
        List<Survivor> survivors = survivorRepository.findAll();
        survivors.forEach(this::initializeCollections);
        return survivors;
    }

    @Transactional
    public Survivor findById(Long id) {
        Survivor survivor = survivorRepository.findById(id).orElseThrow(() -> new SurvivorNotFoundException("Survivor not found"));
        initializeCollections(survivor);
        return survivor;
    }

    // user.getSurvivor() is a lazy placeholder from an earlier transaction and can't be loaded here,
    // so look the survivor up again by id (reading the id doesn't need the placeholder to load).
    @Transactional(readOnly = true)
    public Survivor findByUser(UserProfile user) {
        if (!user.hasSurvivor()) {
            throw new SurvivorNotFoundException("No survivor found for user");
        }
        return findById(user.getSurvivor().getId()); //Also loads skills and gear before transaction ends.
    }

    // open-in-view is off, so lazy collections must be loaded before the transaction ends,
    // otherwise mapping to a DTO in the controller throws LazyInitializationException
    private void initializeCollections(Survivor survivor) {
        Hibernate.initialize(survivor.getSkills());
        Hibernate.initialize(survivor.getGear());
    }

    private void verifyOwnership(UserProfile actor, Survivor survivor) {
        if (!actor.owns(survivor)) {
            throw new ForbiddenException("You do not own this survivor");
        }
    }

    @Transactional
    public void deleteSurvivorById(Long id){
        Survivor survivor = survivorRepository.findById(id)
                .orElseThrow(() -> new SurvivorNotFoundException("Survivor not found"));
        survivorRepository.delete(survivor);
    }

    @Transactional
    public void addSkill(UserProfile actor, Long id, Skill skill) {
        Survivor survivor = survivorRepository.findById(id)
                .orElseThrow(() -> new SurvivorNotFoundException("Survivor not found"));
        verifyOwnership(actor, survivor);
        if (!survivor.getSkills().contains(skill)) {
            survivor.getSkills().add(skill);
            auditEntryService.create(actor, AuditActionType.SKILL_ADDED, "Survivor", id,
                    "Skill " + skill.name() + " added to survivor " + survivor.getName());
        }
    }

    @Transactional
    public void removeSkill(UserProfile actor, Long id, Skill skill) {
        Survivor survivor = survivorRepository.findById(id)
                .orElseThrow(() -> new SurvivorNotFoundException("Survivor not found"));
        verifyOwnership(actor, survivor);
        if (survivor.getSkills().contains(skill)) {
            survivor.getSkills().remove(skill);
            auditEntryService.create(actor, AuditActionType.SKILL_REMOVED, "Survivor", id,
                    "Skill " + skill.name() + " removed from survivor " + survivor.getName());
        }
    }

    @Transactional
    public void loadItem(UserProfile actor, Long id, Item item) {
        Survivor survivor = survivorRepository.findById(id)
                .orElseThrow(() -> new SurvivorNotFoundException("Survivor not found"));
        verifyOwnership(actor, survivor);
        double currentLoad = survivor.getGear().stream()
                .mapToDouble(Item::getWeight)
                .sum();

        if (currentLoad + item.getWeight() > survivor.getMaxLoad()) {
            throw new OverloadedException("Survivor with id: " + id + ", tried to load item with too much weight.");
        }
        survivor.getGear().add(item);
        auditEntryService.create(actor, AuditActionType.ITEM_LOADED, "Survivor", id,
                "Item " + item.getName() + " loaded to survivor " + survivor.getName());
    }

    @Transactional
    public ActionResult performAction(UserProfile actor, Long id, Long actionId) {
        Survivor survivor = survivorRepository.findById(id)
                .orElseThrow(() -> new SurvivorNotFoundException("Survivor not found"));
        verifyOwnership(actor, survivor);
        Action action = actionRepository.findById(actionId)
                .orElseThrow(() -> new ActionNotFoundException("Action not found"));
        double effectiveness = 0.0;

        double strengthContrib = survivor.getAttributes().getStrength() * action.getAttributeWeights().getStrength();
        double agilityContrib = survivor.getAttributes().getAgility() * action.getAttributeWeights().getAgility();
        double trustContrib = survivor.getAttributes().getTrustworthiness()
                * action.getAttributeWeights().getTrustworthiness();
        double intelligenceContrib = survivor.getAttributes().getIntelligence()
                * action.getAttributeWeights().getIntelligence();
        double courageContrib = survivor.getAttributes().getCourage() * action.getAttributeWeights().getCourage();
        double enduranceContrib = survivor.getAttributes().getEndurance() * action.getAttributeWeights().getEndurance();
        double leadershipContrib = survivor.getAttributes().getLeadership()
                * action.getAttributeWeights().getLeadership();

        effectiveness = (strengthContrib + agilityContrib + trustContrib + intelligenceContrib
                + courageContrib + enduranceContrib + leadershipContrib) * 10;

        ActionResult result = new ActionResult(survivor, action, effectiveness);
        auditEntryService.create(actor, AuditActionType.ACTION_PERFORMED, "Survivor", id,
                "Performed action " + action.getName() + " by survivor " + survivor.getName());
        return result;
    }

}
