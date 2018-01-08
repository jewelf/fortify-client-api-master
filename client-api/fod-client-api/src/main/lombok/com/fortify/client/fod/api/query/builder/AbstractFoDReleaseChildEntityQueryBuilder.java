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
package com.fortify.client.fod.api.query.builder;

import com.fortify.client.fod.api.query.FoDEntityQuery;
import com.fortify.client.fod.connection.FoDAuthenticatingRestConnection;

/**
 * This abstract base class is used to build {@link FoDEntityQuery} instances
 * for querying FoD release child entities.
 *  
 * @author Ruud Senden
 *
 * @param <T> Concrete builder type
 */
public abstract class AbstractFoDReleaseChildEntityQueryBuilder<T extends AbstractFoDReleaseChildEntityQueryBuilder<T>> extends AbstractFoDEntityQueryBuilder<T> {

	protected AbstractFoDReleaseChildEntityQueryBuilder(FoDAuthenticatingRestConnection conn, String releaseId, boolean pagingSupported) 
	{
		super(conn, pagingSupported);
		appendPath("/api/v3/releases");
		appendPath(releaseId);
	}
}
