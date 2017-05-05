/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mdtproject15;

import com.opencsv.CSVReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;

/**
 *
 * @author rohan
 */
public class Readings {

    //User ID 1, Site Id 10, Advertiser_id 11, Zip-code
    private static int[] strings = {1, 10, 11, 13};
    //Age 2, gender 3, appCount 4, AppRatingAvg 5, DeviceOS 6, Device_type 7, Device_Model 8, Carrier_Most_Frequent 9, clicks 14, Revenue 15
    private static int[] numbers = {2, 3, 4, 5, 6, 7, 8, 9, 14, 15};

    public static void main(String[] args) {

        String topic = "rbhargav/MDTDATA";

        int qos = 2;
        String broker = "tcp://broker.hivemq.com:1883"; //
        String clientId = "rbhargav";
        MemoryPersistence persistence = new MemoryPersistence();
        System.out.println("Enter");
        try {
            //Create client with broker and client id.
            MqttClient sampleClient = new MqttClient(broker, clientId, persistence);

            //Set the connection options.
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            System.out.println("Connecting to broker: " + broker);

            //Connect client
            sampleClient.connect(connOpts);
            System.out.println("Connected");

            //Read the file
            CSVReader reader;
            reader = new CSVReader(new FileReader(new File("/Users/rohan/Documents/CMU - MISM/Spring/MDT/project/Bidding_Data_700000_Updated.csv")));
            List<String[]> rows = reader.readAll();
            String[] headers = rows.remove(0);

            for (String[] row : rows) {
                JSONObject json = getRow(headers, row);

                System.out.println("Publishing message: " + json);

                //Create mqtt Message with Json String.
                MqttMessage message = new MqttMessage(json.toString().getBytes());

                //Set Quality of Service
                message.setQos(qos);

                //Publish to topic.
                sampleClient.publish(topic, message);
                System.out.println("Message published");

                //Wait for 1 second.
                TimeUnit.MILLISECONDS.sleep(500);
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Readings.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(Readings.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MqttException me) {
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        } finally {
            System.out.println("Disconnected");
            System.exit(0);
        }
    }

    private static JSONObject getRow(String[] headers, String[] row) {
        JSONObject json = new JSONObject();
        //Date 0-0
        json.put("Date", row[0].split(" ")[0]);
        //Time 0-1
        json.put("Time", row[0].split(" ")[1]);
        //Latitude
        json.put("Lat", row[12].split(",")[0]);
        //Longitude
        json.put("Long", row[12].split(",")[1]);

        for (int j : numbers) {
            if (row[j] != null && !row[j].equals("")) {
                if (j == 3) {
                    if (row[j].equals("1")) {
                        json.put(headers[j], "Male");
                    }
                    if (row[j].equals("2")) {
                        json.put(headers[j], "Female");
                    }
                } else {
                    json.put(headers[j], Double.parseDouble(row[j]));
                }
            } else {
                json.put(headers[j], JSONObject.NULL);
            }
        }

        for (int j : strings) {
            if (row[j] != null && !row[j].equals("")) {
                json.put(headers[j], (row[j]));
            } else {
                json.put(headers[j], JSONObject.NULL);
            }
        }
        return json;
    }
}
