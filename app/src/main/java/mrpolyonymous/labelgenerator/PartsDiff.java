/*
Copyright 2024 mrpolyonymous

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package mrpolyonymous.labelgenerator;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Do a diff between two versions of the parts database and a parts collection, and only output
 * the parts that don't exist
 */
public class PartsDiff {

    // Only care about part and colour, not quantity
    record PartColour(Part part, Colour colour) {}
    
    public static MyParts diff(MyParts currentParts, PartsCsvDatabase currentDb, MyParts previousParts, PartsCsvDatabase previousDb) {
        Set<String> oldPartIds = new HashSet<>();
        Set<PartColour> oldPartColours = new HashSet<>();
        
        Set<Part> mappedParts = new HashSet<Part>();
        for (PartColourQuantity pcq : previousParts) {
            oldPartIds.add(pcq.part().id());
            oldPartIds.add(pcq.part().idIgnoringPrint());
            oldPartColours.add(new PartColour(pcq.part(), pcq.colour()));

            // make sure old data exists in current DB
            try {
                if (!mappedParts.contains(pcq.part())) {
                    currentDb.getPartById(pcq.part().id());
                }
            } catch (NoSuchElementException e) {
                e.printStackTrace();
                
                Part oldPart = previousDb.getPartById(pcq.part().id());
                System.err.println("Part with ID " + pcq.part().id() + " not found in current DB. Old part description=" + oldPart.description());
                Part currentPart = currentDb.tryGetPartByDescription(oldPart.description());
                if (currentPart != null) {
                    System.err.println(currentPart);
                } else {
                    currentPart = currentDb.tryGetPartByIdPrefix(pcq.part().id());
                    System.err.println(currentPart);
                }
                mappedParts.add(oldPart);
            }
            // make sure old data exists in current DB
            try {
                currentDb.getColourById(pcq.colour().id());
            } catch (NoSuchElementException e) {
                e.printStackTrace();
                
                Colour oldColour = previousDb.getColourById(pcq.colour().id());
                System.err.println("Colour with ID " + pcq.colour().id() + " not found in current DB. Old colour description=" + oldColour.description());
            }
        }
        
        Set<Part> newParts = new HashSet<>();
        Set<PartColour> newPartColours = new HashSet<>();
        for (PartColourQuantity pcq : currentParts) {
            if (!oldPartIds.contains(pcq.part().id()) && !oldPartIds.contains(pcq.part().idIgnoringPrint())) {
                newParts.add(pcq.part());
            }

            PartColour partColour = new PartColour(pcq.part(), pcq.colour());
            if (!oldPartColours.contains(partColour)) {
                newPartColours.add(partColour);
            }
        }

        System.out.println("Parts with changed IDs: " + mappedParts.size());
        System.out.println("Old part/colour combos: " + oldPartColours.size());
        System.out.println("New parts not in old set or in new colour: " + newPartColours.size());
        
        MyParts onlyNew = new MyParts(currentDb);
        for (PartColourQuantity pcq : currentParts) {
            if (!oldPartIds.contains(pcq.part().id()) && !oldPartIds.contains(pcq.part().idIgnoringPrint())) {
                onlyNew.add(pcq);
            }
        }
        return onlyNew;
    }
}
