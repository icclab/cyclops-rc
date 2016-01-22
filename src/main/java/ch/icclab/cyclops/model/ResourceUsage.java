/*
 * Copyright (c) 2015. Zuercher Hochschule fuer Angewandte Wissenschaften
 *  All Rights Reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may
 *     not use this file except in compliance with the License. You may obtain
 *     a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *     WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *     License for the specific language governing permissions and limitations
 *     under the License.
 */

package ch.icclab.cyclops.model;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Author: Srikanta
 * Created on: 14-Apr-15
 * Description: The class is used for creating the usage report for a resource
 */
public class ResourceUsage {
    private String resourceid;
    private TimeStamp time;
    private ArrayList<String> column;
    private ArrayList<ArrayList<Object>> usage;
    private HashMap tags;

    public String getResourceid() {
        return resourceid;
    }

    public void setResourceid(String resourceid) {
        this.resourceid = resourceid;
    }

    public static class TimeStamp {
        private String from;
        private String to;

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }
    }

    public TimeStamp getTime() {
        return time;
    }

    public void setTime(TimeStamp time) {
        this.time = time;
    }

    public ArrayList<String> getColumn() {
        return column;
    }

    public void setColumn(ArrayList<String> column) {
        this.column = column;
    }

    public ArrayList<ArrayList<Object>> getUsage() {
        return usage;
    }

    public HashMap getTags() {
        return tags;
    }

    public void setTags(HashMap tags) {
        this.tags = tags;
    }

    public void setUsage(ArrayList<ArrayList<Object>> usage) {
        this.usage = usage;
    }
}
