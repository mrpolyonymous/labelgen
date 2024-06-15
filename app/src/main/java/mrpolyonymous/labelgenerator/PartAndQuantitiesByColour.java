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

import java.util.HashMap;
import java.util.Map;

class PartAndQuantitiesByColour {
    private final Part part;
    private final Map<Colour, Integer> colourToQuantity;
    private Colour preferredColour;
    private int quantityOfPreferred;
    
    public PartAndQuantitiesByColour(Part part) {
        this.part = part;
        this.colourToQuantity = new HashMap<>();
    }
    
    public Part part() {
        return part;
    }

    public void addColourAndQuantity(Colour colour, int quantity) {
        if (quantity > quantityOfPreferred) {
            preferredColour = colour;
            quantityOfPreferred = quantity;
        }
        colourToQuantity.put(colour, quantity);
    }
    
    public void setColour(Colour colour) {
        preferredColour = colour;
        quantityOfPreferred = colourToQuantity.getOrDefault(colour, 0);
    }
    
    public Colour colour() {
        return preferredColour;
    }

    public int quantity() {
        return colourToQuantity.values().stream().reduce(0, Integer::sum);
    }
    
}