package service;


import no.loopacademy.exceptions.OverloadedException;
import no.loopacademy.models.items.Item;
import no.loopacademy.models.survivors.Survivor;
import no.loopacademy.models.survivors.SurvivorTypes;
import no.loopacademy.services.SurvivorService;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class SurvivorServiceTest {


    @Test
    void survivorServiceLoadItem_SurvivorIdItem_shouldThrowException(){
        // ARRANGE
        String survivorName = "Kevin";
        double itemWeight = 500000.0;
        SurvivorService service = new SurvivorService();
        Survivor survivor = service.create(survivorName, SurvivorTypes.CAREGIVER);
        Item item = new Item("Medkit", itemWeight);
        long survivorId = survivor.getId();

        // ACT & ASSERT
        assertThrows(OverloadedException.class, () -> {
            service.loadItem(survivorId, item);
        });
    }

    @Test
    void survivorServiceLoadItem_SurvivorIdItem_shouldLoadItemToSurvivor(){
        // ARRANGE
        String survivorName = "Kevin";
        double itemWeight = 5.0;
        Item item = new Item("Medkit", itemWeight);
        List<Item> expextedGearList = new ArrayList<>();
        expextedGearList.add(item);
        SurvivorService service = new SurvivorService();
        Survivor survivor = service.create(survivorName, SurvivorTypes.CAREGIVER);

        long survivorId = survivor.getId();

        // ACT
        service.loadItem(survivorId, item);
        List<Item> actualGear = survivor.getGear();

        // ASSERT
        assertEquals(expextedGearList, actualGear);
    }
}
