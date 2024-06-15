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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Parts downloaded from rebrickable and matched to their corresponding
 * part, colour and quantity
 */
public class MyParts implements Iterable<PartColourQuantity> {

    private final List<PartColourQuantity> allParts;
    private final PartsDatabase partsDatabase;

    public MyParts(PartsDatabase partsDatabase) {
        this.partsDatabase = partsDatabase;
        allParts = new ArrayList<>(8000);
    }

    /**
     * Read data file downloaded from rebrickable My LEGO->All My Parts->Export Parts->Rebrickable CSV
     */
    public void readMyParts(File dataFile) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(dataFile))) {
            String line = br.readLine();
            // skip header  Part,Color,Quantity
            line = br.readLine();
            while (line != null) {
                PartColourQuantity newPart = PartColourQuantity.fromLine(line, partsDatabase);
                allParts.add(newPart);
                line = br.readLine();
            }
        }

        sort();
    }
    
    static final Comparator<PartColourQuantity> SORTER = (p1, p2) -> {
        
        int partIdComp;
        if (p1.part().numericId() != null && p2.part().numericId() != null) {
            partIdComp = p1.part().numericId().compareTo(p2.part().numericId());
        } else {
            partIdComp = p1.part().idIgnoringPrint().compareTo(p2.part().idIgnoringPrint());
        }
        if (partIdComp != 0) {
            return partIdComp;
        }
        return p1.part().id().compareTo(p2.part().id());
    };

    public int size() {
        return allParts.size();
    }

    @Override
    public Iterator<PartColourQuantity> iterator() {
        return Collections.unmodifiableList(allParts).iterator();
    }

    void add(PartColourQuantity pcq) {
        allParts.add(pcq);
    }

    public void sort() {
        allParts.sort(SORTER);
    }
}
