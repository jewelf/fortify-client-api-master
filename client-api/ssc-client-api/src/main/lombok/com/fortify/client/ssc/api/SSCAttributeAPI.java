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

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fortify.client.ssc.annotation.SSCRequiredActionsPermitted;
import com.fortify.client.ssc.api.query.builder.SSCApplicationVersionAttributesQueryBuilder;
import com.fortify.client.ssc.api.query.builder.SSCAttributeDefinitionsQueryBuilder;
import com.fortify.client.ssc.connection.SSCAuthenticatingRestConnection;
import com.fortify.util.rest.json.JSONList;
import com.fortify.util.rest.json.JSONMap;

/**
 * This class is used to access SSC attribute-related functionality.
 * 
 * @author Ruud Senden
 *
 */
public class SSCAttributeAPI extends AbstractSSCAPI {
	public SSCAttributeAPI(SSCAuthenticatingRestConnection conn) {
		super(conn);
	}
	
	public SSCApplicationVersionAttributesQueryBuilder queryApplicationVersionAttributes(String applicationVersionId) {
		return new SSCApplicationVersionAttributesQueryBuilder(conn(), applicationVersionId);
	}
	
	public SSCAttributeDefinitionsQueryBuilder queryAttributeDefinitions() {
		return new SSCAttributeDefinitionsQueryBuilder(conn());
	}
	
	public JSONList getAttributeDefinitions(boolean useCache, String... fields) {
		return queryAttributeDefinitions().useCache(useCache).paramFields(fields==null?null:fields).build().getAll();
	}
	
	public JSONList getApplicationVersionAttributes(String applicationVersionId, boolean useCache, String... fields) {
		return queryApplicationVersionAttributes(applicationVersionId).useCache(useCache).paramFields(fields).build().getAll();
	}
	
	public String getAttributeIdForName(boolean useCache, String attributeName) {
		JSONList attributeDefinitions = getAttributeDefinitions(useCache, "id", "name");
		return attributeDefinitions.mapValue("name", attributeName, "id", String.class);
	}
	
	/**
	 * Get all application version attribute values for the given application version,
	 * indexed by attribute name. Attributes without any value will not be included
	 * in the result.
	 * @param applicationVersionId
	 * @return
	 */
	public JSONMap getApplicationVersionAttributeValuesByName(String applicationVersionId) {
		JSONMap result = new JSONMap();
		JSONList attrs = getApplicationVersionAttributes(applicationVersionId, true, "guid","value","values");
		JSONList attrDefs = getAttributeDefinitions(true, "guid","name");
		for ( JSONMap attr : attrs.asValueType(JSONMap.class) ) {
			String attrName = attrDefs.mapValue("guid", attr.get("guid", String.class), "name", String.class);
			JSONList attrValues = attr.get("values", JSONList.class);
			String attrValue = attr.get("value", String.class);
			if ( StringUtils.isNotBlank(attrValue) ) {
				result.put(attrName, new JSONList(Arrays.asList(attrValue))); 
			} else if ( attrValues!=null && attrValues.size()>0 ) {
				result.put(attrName, new JSONList(attrValues.getValues("name", String.class)));
			}
		}
		return result;
	}
	
	/**
	 * Update the application version attributes as specified in the given 
	 * {@link MultiValueMap} for the given application version id.
	 * @param applicationVersionId
	 * @param attributeNameOrIdToValuesMap
	 */
	@SSCRequiredActionsPermitted({"PUT=/api/v\\d+/projectVersions/\\d+/attributes"})
	public JSONList updateApplicationVersionAttributes(String applicationVersionId, MultiValueMap<String, Object> attributeNameOrIdToValuesMap) {
		JSONList data = new UpdateAttributesJSONMapBuilder(attributeNameOrIdToValuesMap).build();
		
		JSONMap result = conn().executeRequest(HttpMethod.PUT, 
				conn().getBaseResource().path("/api/v1/projectVersions").path(applicationVersionId).path("attributes"), 
				Entity.entity(data, "application/json"), JSONMap.class);
		return result.get("data", JSONList.class);
	}
	
	public MultiValueMap<String, Object> getRequiredAttributesWithDefaultValues() {
		MultiValueMap<String, Object> result = new LinkedMultiValueMap<>();
		JSONList requiredAttributeDefinitions = queryAttributeDefinitions()
				.paramQAnd("required", true).paramFields("id", "type", "category", "appEntityType", "options", "hasDefault").build().getAll();
		for ( JSONMap attributeDefinition : requiredAttributeDefinitions.asValueType(JSONMap.class) ) {
			if ( !"DYNAMIC_SCAN_REQUEST".equals(attributeDefinition.get("category")) 
					&& Arrays.asList("PROJECT_VERSION", "ALL").contains(attributeDefinition.get("appEntityType"))
					&& !attributeDefinition.get("hasDefault", Boolean.class) ) {
				String id = attributeDefinition.get("id", String.class);
				JSONList options = attributeDefinition.get("options", JSONList.class);
				if ( options != null && !options.isEmpty() ) {
					result.add(id, options.mapValue("true", "true", "guid", String.class));
				} else {
					Object value;
					switch ( attributeDefinition.get("type", String.class) ) {
						case "INTEGER": value = 0; break;
						case "BOOLEAN": value = true; break;
						case "DATE": value = new Date(); break;
						default: value = "Auto-filled"; break;
					}
					result.add(id, value);
				}
			}
		}
		return result;
	}
	
	

	/**
	 * Get a {@link Map} containing all attribute definitions indexed by both name and id,
	 * useful for looking up attribute definitions by either name or id. Attribute definitions
	 * that have an 'options' property will also get an 'optionsByNameOrGuid' property,
	 * containing all options indexed by both name and id.
	 * @param useCache
	 * @param fields
	 * @return
	 */
	public JSONMap getAttributeDefinitionsByNameAndId(boolean useCache, String... fields) {
		JSONList attributeDefinitions = getAttributeDefinitions(useCache, fields);
		JSONMap attributeDefinitionsAndNameOrId = attributeDefinitions.toJSONMap("name", String.class, "#this", JSONMap.class);
		attributeDefinitionsAndNameOrId.putAll(attributeDefinitions.toMap("id", String.class, JSONMap.class));
		return attributeDefinitionsAndNameOrId;
	}
	
	private final class UpdateAttributesJSONMapBuilder {
		private final MultiValueMap<String, Object> attributeNameOrIdToValuesMap;
		private final JSONMap attributeDefinitionsByNameOrId;
		public UpdateAttributesJSONMapBuilder(MultiValueMap<String, Object> attributeNameOrIdToValuesMap) {
			this.attributeNameOrIdToValuesMap = attributeNameOrIdToValuesMap;
			this.attributeDefinitionsByNameOrId = getAttributeDefinitionsByNameAndId(false, "id", "name", "type", "options");
		}

		private JSONMap getUpdateAttributeJSONMap(String attributeNameOrId, List<Object> attributeValues) {
			JSONMap result = null;
			JSONMap attributeDefinition = attributeDefinitionsByNameOrId.get(attributeNameOrId, JSONMap.class);
			if ( attributeDefinition == null ) {
				throw new IllegalArgumentException("Attribute name or id "+attributeNameOrId+" does not exist");
			} else {
				result = new JSONMap();
				result.put("attributeDefinitionId", attributeDefinition.get("id"));
				
				String type = attributeDefinition.get("type", String.class);
				
				// Check whether only single (or no) value is given if attribute type is not MULTIPLE 
				if ( !"MULTIPLE".equals(type) && attributeValues!=null && attributeValues.size()>1 ) {
					throw new IllegalArgumentException("Attribute "+attributeNameOrId+" can only contain a single value");
				}
				
				if ( "MULTIPLE".equals(type) || "SINGLE".equals(type) ) {
					result.put("values", getGuidValuesList(attributeDefinition, attributeNameOrId, attributeValues));
					result.put("value", null);
				} else {
					result.put("values", null);
					result.put("value", getSimpleValue(attributeValues));
				}
			}
			return result;
		}

		public Object getSimpleValue(List<Object> attributeValues) {
			Object value = attributeValues == null ? null : attributeValues.get(0);
			if ( value != null ) {
				if ( value instanceof Date ) {
					Date valueDate = (Date)value;
					value = new SimpleDateFormat("yyyy-MM-dd").format(valueDate);
				}
			}
			return value;
		}

		public JSONList getGuidValuesList(JSONMap attributeDefinition, String attributeNameOrId, List<Object> attributeValues) {
			JSONMap optionsByNameAndGuid = attributeDefinition.get("optionsByNameAndGuid", JSONMap.class);
			JSONList values = new JSONList();
			for ( Object optionValueOrGuid : attributeValues ) {
				JSONMap option = optionsByNameAndGuid.get(optionValueOrGuid, JSONMap.class);
				if ( option == null ) {
					throw new IllegalArgumentException("Invalid option "+optionValueOrGuid+" for attribute "+attributeNameOrId);
				} else {
					JSONMap value = new JSONMap();
					value.put("guid", option.get("guid", String.class));
					values.add(value);
				}
			}
			return values;
		}

		public JSONList build() {
			JSONList data = new JSONList();
			for ( Map.Entry<String, List<Object>> entry : attributeNameOrIdToValuesMap.entrySet() ) {
				data.add(getUpdateAttributeJSONMap(entry.getKey(), entry.getValue()));
			}
			return data;
		}
	}

}
