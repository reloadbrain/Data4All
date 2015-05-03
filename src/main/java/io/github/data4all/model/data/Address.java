/*
 * Copyright (c) 2014, 2015 Data4All
 * 
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 * 
 * <p>Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.github.data4all.model.data;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class contains all the informations of an Address e.g road,
 * house_number, postCode, city, country
 * 
 * @author Steeve
 *
 */
public class Address implements Serializable {

    
    private static final long serialVersionUID = -6836778014617785812L;

    // attribute to save a house_number
    private String addresseNr = "";
    // attribute to save a road
    private String road = "";
    // attribute to save a city
    private String city = "";
    // attribute to save a country
    private String country = "";
    // attribute to store a postCode
    private String postCode = "";

    // id of an address
    private int addressId;

    // all getter and setter methods
    public void setAddressId(int addressId) {
        this.addressId = addressId;
    }

    public void setAddresseNr(String addresseNr) {
        this.addresseNr = addresseNr;
    }

    public void setRoad(String road) {
        this.road = road;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public int getAddressId() {
        return addressId;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public String getRoad() {
        return road;
    }

    public String getPostCode() {
        return postCode;
    }

    public String getAddresseNr() {
        return addresseNr;
    }

    /**
     * get a full address
     * @return road + addresseNr + postCode + city + country
     */
    public String getFullAddress() {
        return this.getRoad() + " " + this.getAddresseNr() + " " + this.getPostCode() + " "
                + this.getCity() + " " + this.getCountry();
    }

    @Override
    public boolean equals(Object o) {
        return this.getFullAddress().equals(((Address) o).getFullAddress());
    }

    @Override
    public int hashCode() {
        return this.getFullAddress().hashCode();
    }
    
    /**
     * 
     * @return 
     */
    public String toJson(){
    	JSONObject json=new JSONObject();
    	try {
			json.put("addresseNr", addresseNr);
			json.put("road", road);
			json.put("city", city);
			json.put("postcode", postCode);
			json.put("country",country);
		} catch (JSONException e) {
			e.printStackTrace();
		}
    	return json.toString();
    }
}
