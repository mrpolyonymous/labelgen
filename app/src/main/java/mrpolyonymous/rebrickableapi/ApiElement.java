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
package mrpolyonymous.rebrickableapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * An element retrieved from https://rebrickable.com/api/v3/lego/elements/{element_id}/
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ApiElement {

    // Sample JSON prettified:
//    {
//        "part": {
//          "part_num": "93095",
//          "name": "Panel 1 x 2 x 1 with Rounded Corners and Central Divider",
//          "part_cat_id": 23,
//          "year_from": 2012,
//          "year_to": 2024,
//          "part_url": "https://rebrickable.com/parts/93095/panel-1-x-2-x-1-with-rounded-corners-and-central-divider/",
//          "part_img_url": "https://cdn.rebrickable.com/media/parts/elements/6036409.jpg",
//          "prints": [],
//          "molds": [],
//          "alternates": [],
//          "external_ids": {
//            "BrickLink": [
//              "93095"
//            ],
//            "BrickOwl": [
//              "614680"
//            ],
//            "Brickset": [
//              "18971",
//              "93095"
//            ],
//            "LDraw": [
//              "93095"
//            ],
//            "LEGO": [
//              "18971",
//              "93095"
//            ]
//          },
//          "print_of": null
//        },
//        "color": {
//          "id": 0,
//          "name": "Black",
//          "rgb": "05131D",
//          "is_trans": false,
//          "external_ids": {
//            "BrickLink": {
//              "ext_ids": [
//                11
//              ],
//              "ext_descrs": [
//                [
//                  "Black"
//                ]
//              ]
//            },
//            "BrickOwl": {
//              "ext_ids": [
//                38
//              ],
//              "ext_descrs": [
//                [
//                  "Black"
//                ]
//              ]
//            },
//            "LEGO": {
//              "ext_ids": [
//                26,
//                342
//              ],
//              "ext_descrs": [
//                [
//                  "Black",
//                  "BLACK"
//                ],
//                [
//                  "CONDUCT. BLACK"
//                ]
//              ]
//            },
//            "Peeron": {
//              "ext_ids": [
//                null
//              ],
//              "ext_descrs": [
//                [
//                  "black"
//                ]
//              ]
//            },
//            "LDraw": {
//              "ext_ids": [
//                0,
//                256
//              ],
//              "ext_descrs": [
//                [
//                  "Black"
//                ],
//                [
//                  "Rubber_Black"
//                ]
//              ]
//            }
//          }
//        },
//        "element_id": "6092446",
//        "design_id": "93095",
//        "element_img_url": "https://cdn.rebrickable.com/media/parts/elements/6092446.jpg",
//        "part_img_url": "https://cdn.rebrickable.com/media/parts/elements/6092446.jpg"
//      }
    
    private ApiPart part;
    
    // Too hard to get my brain to spell it "color", but need to match the name in the API
    @JsonProperty("color")
    private ApiColour colour;

    private String elementId;
    private String designId;
    private String elementImgUrl;
    private String partImgUrl;
    public ApiPart getPart() {
        return part;
    }
    public void setPart(ApiPart part) {
        this.part = part;
    }
    public ApiColour getColour() {
        return colour;
    }
    public void setColour(ApiColour colour) {
        this.colour = colour;
    }
    public String getElementId() {
        return elementId;
    }
    public void setElementId(String elementId) {
        this.elementId = elementId;
    }
    public String getDesignId() {
        return designId;
    }
    public void setDesignId(String designId) {
        this.designId = designId;
    }
    public String getElementImgUrl() {
        return elementImgUrl;
    }
    public void setElementImgUrl(String elementImgUrl) {
        this.elementImgUrl = elementImgUrl;
    }
    public String getPartImgUrl() {
        return partImgUrl;
    }
    public void setPartImgUrl(String partImgUrl) {
        this.partImgUrl = partImgUrl;
    }
    
}
