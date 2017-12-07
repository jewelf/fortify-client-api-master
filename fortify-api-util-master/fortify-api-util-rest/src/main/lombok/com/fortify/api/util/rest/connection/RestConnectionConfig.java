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
package com.fortify.api.util.rest.connection;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.glassfish.jersey.client.ClientProperties;

import com.google.common.base.Splitter;

import lombok.Data;

/**
 * This class is used to configure a {@link RestConnection} instance.
 * 
 * @author Ruud Senden
 *
 * @param <T> Type of subclass, used for fluent chaining.
 */
@Data
public class RestConnectionConfig<T extends RestConnectionConfig<T>> implements IRestConnectionConfig {
	private String baseUrl = getDefaultBaseUrl();
	private ProxyConfig proxy = getDefaultProxy();
	private Map<String, Object> connectionProperties = getDefaultConnectionProperties();
	private Credentials credentials = getDefaultCredentials();
	
	/**
	 * Get the {@link CredentialsProvider} to use to authenticate with the
	 * REST service. This default implementation calls {@link #createCredentialsProvider()}
	 * to create a {@link CredentialsProvider} instance, and then sets the
	 * credentials as configured through {@link #setCredentials(Credentials)}
	 * or {@link #credentials(Credentials)} with {@link AuthScope#ANY}.
	 * 
	 * Implementations that use custom authentication mechanisms should override
	 * this method to return null.
	 */
	@Override
	public CredentialsProvider getCredentialsProvider() {
		CredentialsProvider result = createCredentialsProvider();
		result.setCredentials(AuthScope.ANY, credentials);
		return result;
	}
	
	public T baseUrl(String baseUrl) {
		setBaseUrl(baseUrl);
		return getThis();
	}
	
	public T proxy(ProxyConfig proxy) {
		setProxy(proxy);
		return getThis();
	}
	
	public T connectionProperties(String connectionProperties) {
		setConnectionProperties(connectionProperties);
		return getThis();
	}
	
	public T connectionProperties(Map<String, Object> connectionProperties) {
		setConnectionProperties(connectionProperties);
		return getThis();
	}
	
	public T credentials(Credentials credentials) {
		setCredentials(credentials);
		return getThis();
	}
	
	public T credentials(String credentials) {
		setCredentials(credentials);
		return getThis();
	}
	
	public T uri(String uriWithProperties) {
		setUri(uriWithProperties);
		return getThis();
	}
	
	public void setCredentials(Credentials credentials) {
		this.credentials = credentials;
	}
	
	public void setCredentials(String credentialsString) {
		setCredentials(new UsernamePasswordCredentials(credentialsString));
	}
	
	public void setConnectionProperties(Map<String, Object> connectionProperties) {
		this.connectionProperties = connectionProperties;
	}
	
	public void setConnectionProperties(String propertiesString) {
		if ( StringUtils.isNotBlank(propertiesString) ) {
			Map<String, Object> orgProperties = Collections.<String,Object>unmodifiableMap(
					Splitter.on(',').withKeyValueSeparator("=").split(propertiesString));
			Map<String, Object> connectionProperties = new HashMap<String, Object>();
			Map<String,String> propertyKeyReplacementMap = getPropertyKeyReplacementMap();
			for ( Map.Entry<String, Object> entry : orgProperties.entrySet() ) {
				connectionProperties.put(propertyKeyReplacementMap.getOrDefault(entry.getKey(),  entry.getKey()), entry.getValue());
			}
		}
		setConnectionProperties(connectionProperties);
	}
	
	@SuppressWarnings("unchecked")
	protected T getThis() {
		return (T)this;
	}
	
	/**
	 * Create the {@link CredentialsProvider} to use for requests.
	 * This default implementation returns a {@link BasicCredentialsProvider}
	 * instance.
	 * @return
	 */
	protected CredentialsProvider createCredentialsProvider() {
		return new BasicCredentialsProvider();
	}
	
	protected String getDefaultBaseUrl() { return null; }
	protected ProxyConfig getDefaultProxy() { return null; }
	protected Map<String, Object> getDefaultConnectionProperties() { return null; }
	protected Credentials getDefaultCredentials() { return null; }
	
	public void setBaseUrl(String baseUrl) {
		this.baseUrl = validateAndNormalizeUrl(baseUrl);
	}
	
	/**
	 * Validate and normalize the given URL. This will check whether the protocol
	 * is either HTTP or HTTPS, and it will add a trailing slash if necessary.
	 * @param baseUrl
	 * @return The validated and normalized URL
	 */
	protected String validateAndNormalizeUrl(String baseUrl) {
		if (!baseUrl.endsWith("/")) {
			baseUrl = baseUrl+"/";
		}
		if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
			throw new RuntimeException("URL protocol should be either http or https");
		}
		return baseUrl;
	}
	
	public void setUri(String uriWithProperties) {
		String[] parts = uriWithProperties.split(";");
		if ( parts.length > 0 ) {
			URI uri = parseUri(parts[0]);
			setBaseUrl(getBaseUrlFromUri(uri));
			if ( parts.length > 1 ) {
				setConnectionProperties(parts[1]);
			}
			String userInfo = uri.getUserInfo();
			if ( StringUtils.isNotBlank(userInfo) ) {
				setCredentials(userInfo);
			}
		}
		
	}

	private URI parseUri(String uriString) {
		try {
			URI uri = new URI(uriString);
			return uri;
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("Input cannot be parsed as URI: "+uriString);
		}
	}

	protected String getBaseUrlFromUri(URI uri) {
		if ( uri == null ) {
			throw new RuntimeException("URI must be configured");
		}
		try {
			return new URI(uri.getScheme(), null, uri.getHost(), uri.getPort(), uri.getPath(), null, null).toString();
		} catch (URISyntaxException e) {
			throw new RuntimeException("Error constructing URI");
		}
	}

	// TODO add additional property mappings, or provide a more automated way of mapping simple property names to Jersey config properties
	protected Map<String, String> getPropertyKeyReplacementMap() {
		Map<String, String> result = new HashMap<String, String>();
		result.put("connectTimeout", ClientProperties.CONNECT_TIMEOUT);
		result.put("readTimeout", ClientProperties.READ_TIMEOUT);
		return result;
	}
}
