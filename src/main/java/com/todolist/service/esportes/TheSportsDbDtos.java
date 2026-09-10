package com.todolist.service.esportes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public final class TheSportsDbDtos {

    private TheSportsDbDtos() {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EventListResponse {
        @JsonProperty("events")
        private List<Event> events;

        public List<Event> getEvents() {
            return events;
        }

        public void setEvents(List<Event> events) {
            this.events = events;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Event {
        @JsonProperty("idEvent") private String idEvent;
        @JsonProperty("strEvent") private String strEvent;
        @JsonProperty("strSport") private String strSport;
        @JsonProperty("strLeague") private String strLeague;
        @JsonProperty("strHomeTeam") private String strHomeTeam;
        @JsonProperty("strAwayTeam") private String strAwayTeam;
        @JsonProperty("dateEvent") private String dateEvent;
        @JsonProperty("strTime") private String strTime;
        @JsonProperty("strTimestamp") private String strTimestamp;
        @JsonProperty("strVenue") private String strVenue;
        @JsonProperty("strStatus") private String strStatus;
        @JsonProperty("intHomeScore") private String intHomeScore;
        @JsonProperty("intAwayScore") private String intAwayScore;
        @JsonProperty("strThumb") private String strThumb;
        @JsonProperty("strPoster") private String strPoster;

        public String getIdEvent() { return idEvent; }
        public String getStrEvent() { return strEvent; }
        public String getStrSport() { return strSport; }
        public String getStrLeague() { return strLeague; }
        public String getStrHomeTeam() { return strHomeTeam; }
        public String getStrAwayTeam() { return strAwayTeam; }
        public String getDateEvent() { return dateEvent; }
        public String getStrTime() { return strTime; }
        public String getStrTimestamp() { return strTimestamp; }
        public String getStrVenue() { return strVenue; }
        public String getStrStatus() { return strStatus; }
        public String getIntHomeScore() { return intHomeScore; }
        public String getIntAwayScore() { return intAwayScore; }
        public String getStrThumb() { return strThumb; }
        public String getStrPoster() { return strPoster; }
    }
}
