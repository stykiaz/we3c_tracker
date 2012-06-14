package utils;

public class BasicRequests {

	public static class deleteRequest {
		public Long id;
		public deleteRequest() {}
	}
	public static class deleteRequestMongoModel {
		public String id;
		public deleteRequestMongoModel() {}
	}

	public static class filterRequest {
		public String term;
	}
	
	public static class listingRequest {
		public Integer p;
		public String list_order_by;
		public String order_dir = "";
		public Byte resultsPerPage;
		
		protected Integer totalResults = 0;
		protected Integer totalPages = 0;
		protected Byte paginationSectionSize = 6;
		
		public listingRequest() {
			p = 1;
			resultsPerPage = 25;
		}
		
		public void setTotalResults(Integer results) {
			totalResults = results;
			this.totalPages = (int)( Math.ceil( (double)totalResults / (double)resultsPerPage  ) );
		}
		public Integer getTotalPages() {
			return totalPages;
		}
		public Integer getTotalResults() {
			return totalResults;
		}
		public int getNextSetOfPages() {
			if( this.getTotalPages() - p > paginationSectionSize ) 
				return p >= paginationSectionSize ? p + paginationSectionSize : ( paginationSectionSize + 3 <= this.getTotalPages() ? paginationSectionSize + 3 : this.getTotalPages() );
			return this.getTotalPages();
			
		}
		public int getFirstSetOfPages() {
			if( p - paginationSectionSize >= 1 ) return p - paginationSectionSize;
			else return 1;
		}
		public int getResultsPerPage() {
			return resultsPerPage;
		}
		public Integer getCurrentPage () {
			return p;
		}
	}
	
	
	
}
