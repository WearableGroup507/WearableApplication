/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tw.edu.ntust.jojllman.wearableapplication.BLE;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class BraceletGattAttributes {
//    private static HashMap<String, String> attributes = new HashMap();
    public static String NOTIFY = "6E400003-B5A3-F393-E0A9-E50E24DCCA9E";
    public static String WRITE = "6E400002-B5A3-F393-E0A9-E50E24DCCA9E";
    public static String SERVICE = "6E400001-B5A3-F393-E0A9-E50E24DCCA9E";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

//    static {
//        // Sample Services.
//        attributes.put("6E400003-B5A3-F393-E0A9-E50E24DCCA9E", "Heart Rate Service");
//        attributes.put("6E400001-B5A3-F393-E0A9-E50E24DCCA9E", "Battery Service");
//        // Sample Characteristics.
//        attributes.put(NOTIFY, "Notify");
//        attributes.put(SERVICE, "Service");
//        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
//
//    }
//
//    public static String lookup(String uuid, String defaultName) {
//        String name = attributes.get(uuid);
//        return name == null ? defaultName : name;
//    }
}
