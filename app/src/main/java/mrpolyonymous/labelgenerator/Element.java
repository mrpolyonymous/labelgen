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

/**
 * An element definition read from Rebrickable data. Basically an Element
 * is a combination of a part and a colour, with a given ID. Elements
 * are useful because they have dedicated images on Rebrickable. However,
 * not all Part/Colour combinations have an Element in the database, even
 * if they exist in a parts list. 
 */
public record Element(String id, String partId, String colourId, String designId) {

}
