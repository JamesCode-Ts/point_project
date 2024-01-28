package model;

public class HourMarker {

	  private String EntryHour;
	    private String DepartureTime;

	    public HourMarker(String entryHour, String departureTime) {
	        this.EntryHour = entryHour;
	        this.DepartureTime = departureTime;
	    }

		public String getEntryHour() {
			return EntryHour;
		}

		public void setEntryHour(String entryHour) {
			EntryHour = entryHour;
		}

		public String getDepartureTime() {
			return DepartureTime;
		}

		public void setDepartureTime(String departureTime) {
			DepartureTime = departureTime;
		}
}
