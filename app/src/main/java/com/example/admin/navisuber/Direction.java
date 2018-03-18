package com.example.admin.navisuber;

import java.io.Serializable;

/**
 * Created by Admin on 3/15/2018.
 */

public class Direction {
    public Route routes[];
    public String status;

    public Direction(Route[] routes, String status) {
        this.routes = routes;
        this.status = status;
    }

    public Direction() {
    }

    public Route[] getRoutes() {
        return routes;
    }

    public String getStatus() {
        return status;
    }

    public static class Route {
        public Leg legs[];
        public Bound bounds;

        public Bound getBound() {
            return bounds;
        }

        public Leg[] getLegs() {
            return legs;
        }

        public Route(Leg[] legs, Bound bounds) {
            this.legs = legs;
            this.bounds = bounds;
        }
    }

    public static class Bound{
        public Northeast northeast;
        public Southwest southwest;

        public Bound(Northeast northeast, Southwest southwest) {
            this.northeast = northeast;
            this.southwest = southwest;
        }

        public Northeast getNortheast() {
            return northeast;
        }

        public Southwest getSouthwest() {
            return southwest;
        }

        public static class Northeast {
            public double lat;
            public double lng;

            public Northeast(double lat, double lng) {
                this.lat = lat;
                this.lng = lng;
            }

            public double getLat() {
                return lat;
            }

            public double getLng() {
                return lng;
            }
        }

        public static class Southwest{
            public double lat;
            public double lng;

            public Southwest(double lat, double lng) {
                this.lat = lat;
                this.lng = lng;
            }

            public double getLat() {
                return lat;
            }

            public double getLng() {
                return lng;
            }

        }
    }

    public static class Leg {
        public start_location start_location;
        public end_location end_location;
        public String start_address;
        public String destinationAddress;
        public Step steps[];
        public Distance distance;
        public Duration duration;

        public Leg(start_location start_location, end_location end_location, String start_address, String end_address,
                   Step[] steps, Distance distance, Duration duration) {
            this.start_location = start_location;
            this.end_location = end_location;
            this.start_address = start_address;
            this.destinationAddress = end_address;
            this.steps = steps;
            this.distance = distance;
            this.duration = duration;
        }

        public start_location getstart_location() {
            return start_location;
        }

        public end_location getend_location() {
            return end_location;
        }

        public String getstart_address() {
            return start_address;
        }

        public String getDestinationAddress() {
            return destinationAddress;
        }

        public Distance getDistance() {
            return distance;
        }

        public Duration getDuration() {
            return duration;
        }

        public Step[] getSteps() {
            return steps;
        }

        public static class Distance{
            String text;
            String value;

            public String getText() {
                return text;
            }

            public String getValue() {
                return value;
            }

            public Distance(String text, String value) {

                this.text = text;
                this.value = value;
            }
        }

        public static class Duration{
            String text;
            String value;

            public Duration(String value, String text) {
                this.value = value;
                this.text = text;
            }

            public String getText() {

                return text;
            }

            public String getValue() {
                return value;
            }

        }
        public static class Step {
            public end_location end_location;
            public start_location start_location;
            public String html_instructions;
            public String travel_mode;
            public String maneuver;

            public Step(end_location end_location, start_location start_location, String htmlInstruction, String travel_mode, String maneuver) {
                this.end_location = end_location;
                this.start_location = start_location;
                this.html_instructions = htmlInstruction;
                this.travel_mode = travel_mode;
                this.maneuver = maneuver;
            }

            public end_location getend_location() {
                return end_location;
            }

            public start_location getstart_location() {
                return start_location;
            }

            public String getHtmlInstruction() {
                return html_instructions;
            }

            public String gettravel_mode() {
                return travel_mode;
            }

            public String getManeuver() {
                return maneuver;
            }
        }

        public static class start_location {
            public double lat;
            public double lng;

            public start_location(double lat, double lng) {
                this.lat = lat;
                this.lng = lng;
            }

            public double getLat() {
                return lat;
            }

            public void setLat(double lat) {
                this.lat = lat;
            }

            public double getLng() {
                return lng;
            }

            public void setLng(double lng) {
                this.lng = lng;
            }
        }

        public static class end_location {
            public double lat;
            public double lng;

            public end_location(double lat, double lng) {
                this.lat = lat;
                this.lng = lng;
            }

            public double getLat() {
                return lat;
            }

            public double getLng() {
                return lng;
            }
        }

    }
}
