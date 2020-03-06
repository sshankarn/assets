package com.ibm;


import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import com.ibm.wmlconnector.COSConnector;
import com.ibm.wmlconnector.WMLJob;
import com.ibm.wmlconnector.impl.COSConnectorImpl;
import com.ibm.wmlconnector.impl.WMLConnectorImpl;
import org.json.JSONArray;
import org.json.JSONException;

public class Sample {
    private static final Logger LOGGER = Logger.getLogger(Sample.class.getName());

    private static final String WML_URL = "https://us-south.ml.cloud.ibm.com";
    private static final String WML_APIKEY  = "XXXXXXXXXXXXXXXXXXXXXXXXXXX";
    private static final String WML_INSTANCE_ID = "XXXXXXXXXXXXXXXXXXXXXXXXXXX";

    private static final String COS_APIKEY  = "XXXXXXXXXXXXXXXXXXXXXXXXXXX";
    private static final String COS_ACCESS_KEY_ID = "XXXXXXXXXXXXXXXXXXXXXXXXXXX";
    private static final String COS_SECRET_ACCESS_KEY = "XXXXXXXXXXXXXXXXXXXXXXXXXXX";


    private static final String COS_ENDPOINT = "https://s3.eu-gb.cloud-object-storage.appdomain.cloud";
    private static final String COS_BUCKET = "test-lp";

    private static JSONArray getDietData() {
        String data = "[\n" +
                "				{\n" +
                "					\"id\":\"diet_food.csv\",\n" +
                "					\"fields\" : [\"name\",\"unit_cost\",\"qmin\",\"qmax\"],\n" +
                "					\"values\" : [\n" +
                "						[\"Roasted Chicken\", 0.84, 0, 10],\n" +
                "						[\"Spaghetti W/ Sauce\", 0.78, 0, 10],\n" +
                "						[\"Tomato,Red,Ripe,Raw\", 0.27, 0, 10],\n" +
                "						[\"Apple,Raw,W/Skin\", 0.24, 0, 10],\n" +
                "						[\"Grapes\", 0.32, 0, 10],\n" +
                "						[\"Chocolate Chip Cookies\", 0.03, 0, 10],\n" +
                "						[\"Lowfat Milk\", 0.23, 0, 10],\n" +
                "						[\"Raisin Brn\", 0.34, 0, 10],\n" +
                "						[\"Hotdog\", 0.31, 0, 10]\n" +
                "					]\n" +
                "				},\n" +
                "				{\n" +
                "					\"id\":\"diet_food_nutrients.csv\",\n" +
                "					\"fields\" : [\"Food\",\"Calories\",\"Calcium\",\"Iron\",\"Vit_A\",\"Dietary_Fiber\",\"Carbohydrates\",\"Protein\"],\n" +
                "					\"values\" : [\n" +
                "						[\"Spaghetti W/ Sauce\", 358.2, 80.2, 2.3, 3055.2, 11.6, 58.3, 8.2],\n" +
                "						[\"Roasted Chicken\", 277.4, 21.9, 1.8, 77.4, 0, 0, 42.2],\n" +
                "						[\"Tomato,Red,Ripe,Raw\", 25.8, 6.2, 0.6, 766.3, 1.4, 5.7, 1],\n" +
                "						[\"Apple,Raw,W/Skin\", 81.4, 9.7, 0.2, 73.1, 3.7, 21, 0.3],\n" +
                "						[\"Grapes\", 15.1, 3.4, 0.1, 24, 0.2, 4.1, 0.2],\n" +
                "						[\"Chocolate Chip Cookies\", 78.1, 6.2, 0.4, 101.8, 0, 9.3, 0.9],\n" +
                "						[\"Lowfat Milk\", 121.2, 296.7, 0.1, 500.2, 0, 11.7, 8.1],\n" +
                "						[\"Raisin Brn\", 115.1, 12.9, 16.8, 1250.2, 4, 27.9, 4],\n" +
                "						[\"Hotdog\", 242.1, 23.5, 2.3, 0, 0, 18, 10.4	]\n" +
                "					]\n" +
                "				},\n" +
                "				{\n" +
                "					\"id\":\"diet_nutrients.csv\",\n" +
                "					\"fields\" : [\"name\",\"qmin\",\"qmax\"],\n" +
                "					\"values\" : [\n" +
                "						[\"Calories\", 2000, 2500],\n" +
                "						[\"Calcium\", 800, 1600],\n" +
                "						[\"Iron\", 10, 30],\n" +
                "						[\"Vit_A\", 5000, 50000],\n" +
                "						[\"Dietary_Fiber\", 25, 100],\n" +
                "						[\"Carbohydrates\", 0, 300],\n" +
                "						[\"Protein\", 50, 100]\n" +
                "					]\n" +
                "				}\n" +
                "			],\n";
        JSONArray jsonData  = new JSONArray(data);
        return jsonData;

    }



    public void createAndRunJobOnExistingDeployment(String deployment_id, JSONArray input_data, JSONArray output_data_references) {

        LOGGER.info("Create and run job");

        WMLConnectorImpl wml = new WMLConnectorImpl(WML_URL, WML_INSTANCE_ID, WML_APIKEY);
        WMLJob job  = wml.createJob(deployment_id, input_data, output_data_references);
        String state = null;
        do {
            try {
                Thread.sleep(1000);
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

            job.updateStatus();

            try {
                state = job.getState();
                if (job.hasSolveState()) {
                    HashMap<String, Object> kpis = job.getKPIs();

                    Iterator<String> keys = kpis.keySet().iterator();

                    while (keys.hasNext()) {
                        String kpi = keys.next();
                        LOGGER.info("KPI " + kpi + " = " + kpis.get(kpi));
                    }
                }
            } catch (JSONException e) {
                LOGGER.severe("Error extractState: " + e);
            }

            LOGGER.info("State: " + state);
        } while (!state.equals("completed") && !state.equals("failed"));
        if (state.equals("failed"))
            LOGGER.severe("Job failed.");
        else {
            JSONArray output_data = job.extractOutputData();
            LOGGER.info("output_data = " + output_data);
        }

    }


    public String createAndDeployDietPythonModel() {

        LOGGER.info("Create Pyhton Model");

        WMLConnectorImpl wml = new WMLConnectorImpl(WML_URL, WML_INSTANCE_ID, WML_APIKEY);

        String model_id = wml.createNewModel("Diet","do-docplex_12.9","src/resources/diet.zip");
        LOGGER.info("model_id = "+ model_id);

        String deployment_id = wml.deployModel("diet-test-wml-2", wml.getModelHref(model_id, false),"S",1);
        LOGGER.info("deployment_id = "+ deployment_id);

        return deployment_id;
    }

    public void deleteDeployment(String deployment_id) {

        LOGGER.info("Delete deployment");


        WMLConnectorImpl wml = new WMLConnectorImpl(WML_URL, WML_INSTANCE_ID, WML_APIKEY);
        wml.deleteDeployment(deployment_id);

    }

    public void createDeployAndRunDietPythonModel() {

        LOGGER.info("Full flow with Diet");

        String deployment_id = createAndDeployDietPythonModel();
        JSONArray input_data = getDietData();
        createAndRunJobOnExistingDeployment(deployment_id, input_data, null);

    }

    public void fullDietFlow(boolean useOutputDataReferences) {

        LOGGER.info("Full flow with Diet");

        String deployment_id = createAndDeployDietPythonModel();
        JSONArray input_data = getDietData();
        JSONArray output_data_references = null;
        if (useOutputDataReferences) {
            COSConnector cos = new COSConnectorImpl(COS_ENDPOINT, COS_APIKEY, COS_BUCKET, COS_ACCESS_KEY_ID, COS_SECRET_ACCESS_KEY);
            output_data_references = new JSONArray();
            output_data_references.put(cos.getOutputDataReferences("log.txt"));
        }
        createAndRunJobOnExistingDeployment(deployment_id, input_data, output_data_references);
        deleteDeployment(deployment_id);
    }

    public void getLog() {

        LOGGER.info("Get log");
        COSConnector cos = new COSConnectorImpl(COS_ENDPOINT, COS_APIKEY, COS_BUCKET, COS_ACCESS_KEY_ID, COS_SECRET_ACCESS_KEY);
        String log = cos.getFile("log.txt");

        log = log.replaceAll("\\r", "\n");
        LOGGER.info("Log: " + log);
    }

    public static void main(String[] args) {
        Sample main = new Sample();

        //main.createAndDeployDietPythonModel();


 /*
        String deployment_id = "c23b824d-80f8-4117-8441-1313a9ad77f9";
        JSONArray input_data = getDietData();
        COSConnector cos = new COSConnectorImpl(COS_ENDPOINT, COS_APIKEY, COS_BUCKET, COS_ACCESS_KEY_ID, COS_SECRET_ACCESS_KEY);
        JSONArray output_data_references = new JSONArray();
        output_data_references.put(cos.getOutputDataReferences("log.txt"));
        main.createAndRunJobOnExistingDeployment(deployment_id, input_data, output_data_references);

        main.getLog();
*/
//        main.deleteDeployment(deployment_id);

        main.fullDietFlow(true);
        main.getLog();


    }
}