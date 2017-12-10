/*******************************************************************************
 * (c) Copyright 2017 EntIT Software LLC
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
package com.fortify.api.ssc.connection.api.query.builder;

import com.fortify.api.ssc.connection.SSCAuthenticatingRestConnection;
import com.fortify.api.ssc.connection.api.query.builder.param.SSCParamFields;
import com.fortify.api.ssc.connection.api.query.builder.param.SSCParamOrderBy;
import com.fortify.api.ssc.connection.api.query.builder.param.SSCParamQ;

public final class SSCJobsQueryBuilder extends AbstractSSCEntityQueryBuilder<SSCJobsQueryBuilder> {
	private final SSCParamFields paramFields = add(new SSCParamFields());
	private final SSCParamOrderBy paramOrderBy = add(new SSCParamOrderBy());
	private final SSCParamQ paramQ = add(new SSCParamQ());
	
	public SSCJobsQueryBuilder(SSCAuthenticatingRestConnection conn) {
		super(conn, true);
	}

	public final SSCJobsQueryBuilder paramFields(String... fields) {
		paramFields.paramFields(fields); return _this();
	}

	public final SSCJobsQueryBuilder orderBy(String orderBy) {
		paramOrderBy.orderBy(orderBy); return _this();
	}

	public final SSCJobsQueryBuilder paramQAnd(String field, String value) {
		paramQ.paramQAnd(field, value); return _this();
	}

	public final SSCJobsQueryBuilder id(String id) {
		return paramQAnd("id", id);
	}

	public final SSCJobsQueryBuilder jobClassName(String jobClassName) {
		return paramQAnd("jobClassName", jobClassName);
	}

	public final SSCJobsQueryBuilder priority(int priority) {
		return paramQAnd("priority", "" + priority);
	}

	public final SSCJobsQueryBuilder state(String state) {
		return paramQAnd("state", state);
	}
	
	@Override
	protected String getTargetPath() {
		return "/api/v1/jobs";
	}
}