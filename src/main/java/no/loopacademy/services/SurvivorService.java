package no.loopacademy.services;

import no.loopacademy.exceptions.ActionNotFoundException;
import no.loopacademy.exceptions.OverloadedException;
import java.util.List;

import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import no.loopacademy.exceptions.SurvivorNotFoundException;
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

@Service
public class SurvivorService {

    private final SurvivorRepository survivorRepository;
    private final ActionRepository actionRepository;
    private final UserProfileService userProfileService;
    private final AuditEntryService auditEntryService;

    public SurvivorService(SurvivorRepository survivorRepository, ActionRepository actionRepository,
            UserProfileService userProfileService, AuditEntryService auditEntryService) {
        this.survivorRepository = survivorRepository;
        this.actionRepository = actionRepository;
        this.userProfileService = userProfileService;
        this.auditEntryService = auditEntryService;
    }

    @Transactional
    public Survivor create(String keycloakId, String name, SurvivorType type) {
        UserProfile actor = userProfileService.findByKeycloakId(keycloakId);
        Survivor survivor = survivorRepository.save(new Survivor(name, type));
        auditEntryService.create(actor, AuditActionType.SURVIVOR_CREATED, "Survivor", survivor.getId(),
                "Created " + type + " survivor " + name);
        return survivor;
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

    // open-in-view is off, so lazy collections must be loaded before the transaction ends,
    // otherwise mapping to a DTO in the controller throws LazyInitializationException
    private void initializeCollections(Survivor survivor) {
        Hibernate.initialize(survivor.getSkills());
        Hibernate.initialize(survivor.getGear());
    }

    @Transactional
    public void addSkill(String keycloakId, Long id, Skill skill) {
        UserProfile actor = userProfileService.findByKeycloakId(keycloakId);
        Survivor survivor = survivorRepository.findById(id)
                .orElseThrow(() -> new SurvivorNotFoundException("Survivor not found"));
        if (!survivor.getSkills().contains(skill)) {
            survivor.getSkills().add(skill);
            auditEntryService.create(actor, AuditActionType.SKILL_ADDED, "Survivor", id,
                    "Skill " + skill.name() + " added to survivor " + survivor.getName());
        }
    }

    @Transactional
    public void removeSkill(String keycloakId, Long id, Skill skill) {
        UserProfile actor = userProfileService.findByKeycloakId(keycloakId);
        Survivor survivor = survivorRepository.findById(id)
                .orElseThrow(() -> new SurvivorNotFoundException("Survivor not found"));
        if (survivor.getSkills().contains(skill)) {
            survivor.getSkills().remove(skill);
            auditEntryService.create(actor, AuditActionType.SKILL_REMOVED, "Survivor", id,
                    "Skill " + skill.name() + " removed from survivor " + survivor.getName());
        }
    }

    @Transactional
    public void loadItem(String keycloakId, Long id, Item item) {
        UserProfile actor = userProfileService.findByKeycloakId(keycloakId);
        Survivor survivor = survivorRepository.findById(id)
                .orElseThrow(() -> new SurvivorNotFoundException("Survivor not found"));
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
    public ActionResult performAction(String keycloakId, Long id, Long actionId) {
        UserProfile actor = userProfileService.findByKeycloakId(keycloakId);
        Survivor survivor = survivorRepository.findById(id)
                .orElseThrow(() -> new SurvivorNotFoundException("Survivor not found"));
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
