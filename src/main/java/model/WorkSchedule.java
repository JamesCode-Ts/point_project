package model;


public class WorkSchedule {
	
	 private String EntryHour;
	    private String DepartureTime;

	    public WorkSchedule(String EntryHour, String DepartureTime) {
	        this.EntryHour = EntryHour;
	        this.DepartureTime = DepartureTime;
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
		
		  @Override
		    public String toString() {
		        return "WorkSchedule{" +
		                "entryHour='" + EntryHour + '\'' +
		                ", departureTime='" + DepartureTime + '\'' +
		                '}';
		    }
		  
}