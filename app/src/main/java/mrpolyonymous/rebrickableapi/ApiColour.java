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

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * A colour retrieved from https://rebrickable.com/api/v3/lego/colors/{id}/
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ApiColour {

    // Sample JSON prettified
//    {
//        "id": 0,
//        "name": "Black",
//        "rgb": "05131D",
//        "is_trans": false,
//        "external_ids": {
//          "BrickLink": {
//            "ext_ids": [
//              11
//            ],
//            "ext_descrs": [
//              [
//                "Black"
//              ]
//            ]
//          },
//          "BrickOwl": {
//            "ext_ids": [
//              38
//            ],
//            "ext_descrs": [
//              [
//                "Black"
//              ]
//            ]
//          },
//          "LEGO": {
//            "ext_ids": [
//              26,
//              342
//            ],
//            "ext_descrs": [
//              [
//                "Black",
//                "BLACK"
//              ],
//              [
//                "CONDUCT. BLACK"
//              ]
//            ]
//          },
//          "Peeron": {
//            "ext_ids": [
//              null
//            ],
//            "ext_descrs": [
//              [
//                "black"
//              ]
//            ]
//          },
//          "LDraw": {
//            "ext_ids": [
//              0,
//              256
//            ],
//            "ext_descrs": [
//              [
//                "Black"
//              ],
//              [
//                "Rubber_Black"
//              ]
//            ]
//          }
//        }
//      }    
    private int id;
    private String name;
    private String rgb;
    // Don't let JK Rowling see this
    @JsonProperty("is_trans")
    private boolean trans;
    private Map<String, Object> externalIds;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRgb() {
        return rgb;
    }

    public void setRgb(String rgb) {
        this.rgb = rgb;
    }

    public boolean isTrans() {
        return trans;
    }

    public void setTrans(boolean trans) {
        this.trans = trans;
    }

    public Map<String, Object> getExternalIds() {
        return externalIds;
    }

    public void setExternalIds(Map<String, Object> externalIds) {
        this.externalIds = externalIds;
    }

}
