/*******************************************************************************
 * (c) Copyright 2017 EntIT Software LLC, a Micro Focus company
 *
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY 
 * KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE 
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * IN THE SOFTWARE.
 ******************************************************************************/
package com.fortify.client.ssc.api;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;

import com.fortify.client.ssc.annotation.SSCRequiredActionsPermitted;
import com.fortify.client.ssc.api.query.builder.SSCApplicationVersionIssuesQueryBuilder;
import com.fortify.client.ssc.api.query.builder.SSCIssueDetailsByIdQueryBuilder;
import com.fortify.client.ssc.connection.SSCAuthenticatingRestConnection;
import com.fortify.util.rest.json.JSONList;
import com.fortify.util.rest.json.JSONMap;

/**
 * This class is used to access SSC issue-related functionality.
 * 
 * @author Ruud Senden
 *
 */
public class SSCIssueAPI extends AbstractSSCAPI {
	public SSCIssueAPI(SSCAuthenticatingRestConnection conn) {
		super(conn);
	}
	
	public SSCApplicationVersionIssuesQueryBuilder queryIssues(String applicationVersionId) {
		return new SSCApplicationVersionIssuesQueryBuilder(conn(), applicationVersionId);
	}
	
	public SSCIssueDetailsByIdQueryBuilder queryIssueDetailsById(String issueId) {
		return new SSCIssueDetailsByIdQueryBuilder(conn(), issueId);
	}
	
	public JSONMap getIssueDetails(String issueId, boolean useCache, String... fields) {
		return queryIssueDetailsById(issueId).useCache(useCache).paramFields(fields).build().getUnique();
	}
	
	/**
	 * Update the issue search options for the given application version 
	 * @param applicationVersionId
	 * @param issueSearchOptions
	 */
	@SSCRequiredActionsPermitted({"PUT=/api/v\\d+/projectVersions/\\d+/issueSearchOptions"})
	public void updateApplicationVersionIssueSearchOptions(String applicationVersionId, IssueSearchOptions issueSearchOptions) {
		conn().executeRequest(HttpMethod.PUT, 
				conn().getBaseResource().path("/api/v1/projectVersions")
				.path(applicationVersionId).path("issueSearchOptions"),
				Entity.entity(issueSearchOptions.getJSONRequestData(), "application/json"), JSONMap.class);
	}
	
	/**
	 * Validate the given issue search string.
	 */
	@SSCRequiredActionsPermitted({"POST=/api/v\\d+/validateSearchString"})
	public JSONMap validateIssueSearchString(String searchString) {
		JSONMap request = new JSONMap();
		request.put("stringToValidate", searchString);
		return conn().executeRequest(HttpMethod.POST, 
				conn().getBaseResource().path("/api/v1/validateSearchString"), 
				Entity.entity(request, "application/json"), JSONMap.class).get("data", JSONMap.class);
	}
	
	/**
	 * This class describes the SSC issue search options, allowing to either 
	 * include or exclude removed, hidden and suppressed issues.
	 * @author Ruud Senden
	 *
	 */
	public static class IssueSearchOptions {
		private Map<String, Boolean> searchOptions = new HashMap<String, Boolean>();
		
		public boolean isIncludeRemoved() {
			return searchOptions.getOrDefault("REMOVED", false);
		}
		public void setIncludeRemoved(boolean includeRemoved) {
			searchOptions.put("REMOVED", includeRemoved);
		}
		public boolean isIncludeSuppressed() {
			return searchOptions.getOrDefault("SUPPRESSED", false);
		}
		public void setIncludeSuppressed(boolean includeSuppressed) {
			searchOptions.put("SUPPRESSED", includeSuppressed);
		}
		public boolean isIncludeHidden() {
			return searchOptions.getOrDefault("HIDDEN", false);
		}
		public void setIncludeHidden(boolean includeHidden) {
			searchOptions.put("HIDDEN", includeHidden);
		}
		
		JSONList getJSONRequestData() {
			JSONList result = new JSONList();
			result.add(getOption("REMOVED"));
			result.add(getOption("SUPPRESSED"));
			result.add(getOption("HIDDEN"));
			return result;
		}
		private JSONMap getOption(String optionType) {
			JSONMap result = new JSONMap();
			result.put("optionType", optionType);
			result.put("optionValue", searchOptions.getOrDefault(optionType, false));
			return result;
		}
	}
}
