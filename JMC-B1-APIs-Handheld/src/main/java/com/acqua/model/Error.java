package com.acqua.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Error {
		private Integer code;
		
		public Error() {
			super();
		}
		
		public Error(Integer code) {
			super();
			this.code = code;
		}
		
		public Integer getCode() {
			return code;
		}
		
		public void setCode(Integer code) {
			this.code = code;
		}
}
