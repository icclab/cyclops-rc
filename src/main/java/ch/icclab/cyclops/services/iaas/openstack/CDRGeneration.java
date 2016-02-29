package ch.icclab.cyclops.services.iaas.openstack;

import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.schedule.runner.AbstractRunner;
import org.restlet.resource.ClientResource;

/**
 * Copyright (c) 2015. Zuercher Hochschule fuer Angewandte Wissenschaften
 * All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 * <p>
 * Created by Manu Perez on 28/02/16.
 */

public class CDRGeneration extends AbstractRunner{

    @Override
    public void run() {
        generateRate();
        generateCDR();
    }

    /**
     * This method will call to the /generate/cdr api in order to activate the computation of the new cdr.
     */
    private void generateCDR(){
        ClientResource clientResource = new ClientResource(Loader.getSettings().getCyclopsSettings().getRcServiceUrl()+"/generate/cdr");
        clientResource.get();
    }


    private void generateRate(){
        ClientResource clientResource = new ClientResource(Loader.getSettings().getCyclopsSettings().getRcServiceUrl()+"/generate/rate");
        clientResource.get();
    }
}
