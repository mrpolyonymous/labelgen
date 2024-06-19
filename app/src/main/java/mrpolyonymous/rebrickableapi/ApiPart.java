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

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * A part retrieved from https://rebrickable.com/api/v3/lego/parts/{part_num}/
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ApiPart {

    // Sample JSON prettified
//    {
//        "part_num": "93095",
//        "name": "Panel 1 x 2 x 1 with Rounded Corners and Central Divider",
//        "part_cat_id": 23,
//        "year_from": 2012,
//        "year_to": 2024,
//        "part_url": "https://rebrickable.com/parts/93095/panel-1-x-2-x-1-with-rounded-corners-and-central-divider/",
//        "part_img_url": "https://cdn.rebrickable.com/media/parts/elements/6036409.jpg",
//        "prints": [],
//        "molds": [],
//        "alternates": [],
//        "external_ids": {
//          "BrickLink": [
//            "93095"
//          ],
//          "BrickOwl": [
//            "614680"
//          ],
//          "Brickset": [
//            "18971",
//            "93095"
//          ],
//          "LDraw": [
//            "93095"
//          ],
//          "LEGO": [
//            "18971",
//            "93095"
//          ]
//        },
//        "print_of": null
//      }
    
    /**
     * The part number, aka part identifier. These aren't always numbers, would have been nice
     * if the API schema called this "part ID" instead but such is life.
     */
    private String partNum;
    private String name;
    private long partCatId;
    private long yearFrom;
    private long yearTo;
    private String partUrl;
    private String partImgUrl;
    private List<String> prints;
    private List<String> molds;
    private List<String> alternates;
    private Map<String, Object> externalIds;
    
    private String printOf;

    public String getPartNum() {
        return partNum;
    }

    public void setPartNum(String partNum) {
        this.partNum = partNum;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getPartCatId() {
        return partCatId;
    }

    public void setPartCatId(long partCatId) {
        this.partCatId = partCatId;
    }

    public long getYearFrom() {
        return yearFrom;
    }

    public void setYearFrom(long yearFrom) {
        this.yearFrom = yearFrom;
    }

    public long getYearTo() {
        return yearTo;
    }

    public void setYearTo(long yearTo) {
        this.yearTo = yearTo;
    }

    public String getPartUrl() {
        return partUrl;
    }

    public void setPartUrl(String partUrl) {
        this.partUrl = partUrl;
    }

    public String getPartImgUrl() {
        return partImgUrl;
    }

    public void setPartImgUrl(String partImgUrl) {
        this.partImgUrl = partImgUrl;
    }

    public List<String> getPrints() {
        return prints;
    }

    public void setPrints(List<String> prints) {
        this.prints = prints;
    }

    public List<String> getMolds() {
        return molds;
    }

    public void setMolds(List<String> molds) {
        this.molds = molds;
    }

    public List<String> getAlternates() {
        return alternates;
    }

    public void setAlternates(List<String> alternates) {
        this.alternates = alternates;
    }

    public Map<String, Object> getExternalIds() {
        return externalIds;
    }

    public void setExternalIds(Map<String, Object> externalIds) {
        this.externalIds = externalIds;
    }

    public String getPrintOf() {
        return printOf;
    }

    public void setPrintOf(String printOf) {
        this.printOf = printOf;
    }

    
    
}
