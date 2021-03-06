package jas.spawner.refactor.configsloader;

import jas.common.helper.GsonHelper;
import jas.spawner.refactor.biome.BiomeGroupBuilder;
import jas.spawner.refactor.configsloader.ConfigLoader.VersionedFile;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.google.common.base.Optional;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class BiomeSettingsLoader implements VersionedFile {
	private String fileVersion;

	public final TreeMap<String, String> biomeMappings;
	private Optional<TreeMap<String, TreeMap<String, BiomeGroupBuilder>>> configNameToAttributeGroups;
	private Optional<TreeMap<String, TreeMap<String, BiomeGroupBuilder>>> configNameToBiomeGroups;

	public BiomeSettingsLoader() {
		fileVersion = Serializer.FILE_VERSION;
		this.biomeMappings = new TreeMap<String, String>();
		this.configNameToAttributeGroups = Optional.absent();
		this.configNameToBiomeGroups = Optional.absent();
	}

	public BiomeSettingsLoader(Map<String, String> biomeMappings, Collection<BiomeGroupBuilder> attributeGroups,
			Collection<BiomeGroupBuilder> biomeGroups) {
		this.biomeMappings = new TreeMap<String, String>(biomeMappings);
		this.configNameToBiomeGroups = Optional.of(new TreeMap<String, TreeMap<String, BiomeGroupBuilder>>());
		this.configNameToAttributeGroups = Optional.of(new TreeMap<String, TreeMap<String, BiomeGroupBuilder>>());
		for (BiomeGroupBuilder group : attributeGroups) {
			getOrCreate(configNameToAttributeGroups.get(), group.getConfigName()).put(group.getGroupID(), group);
		}
	}

	public Optional<TreeMap<String, TreeMap<String, BiomeGroupBuilder>>> getConfigNameToAttributeGroups() {
		return configNameToAttributeGroups;
	}

	public Optional<TreeMap<String, TreeMap<String, BiomeGroupBuilder>>> getConfigNameToBiomeGroups() {
		return configNameToBiomeGroups;
	}

	private TreeMap<String, BiomeGroupBuilder> getOrCreate(TreeMap<String, TreeMap<String, BiomeGroupBuilder>> map,
			String key) {
		TreeMap<String, BiomeGroupBuilder> group = map.get(key);
		if (group == null) {
			group = new TreeMap<String, BiomeGroupBuilder>();
			map.put(key, group);
		}
		return group;
	}

	@Override
	public String getVersion() {
		return fileVersion;
	}

	public static class Serializer implements JsonSerializer<BiomeSettingsLoader>, JsonDeserializer<BiomeSettingsLoader> {
		public static final String FILE_VERSION = "2.0";
		public final String FILE_VERSION_KEY = "FILE_VERSION";
		public final String BIOME_MAPPINGS = "Biome Mappings";
		public final String ATTRIBUTE_GROUPS = "Attribute Groups";
		// public final String CONTENTS_KEY = "contents";

		@Deprecated
		public final String BIOME_GROUPS = "Biome Groups";

		@Override
		public JsonElement serialize(BiomeSettingsLoader saveObject, Type type, JsonSerializationContext context) {
			JsonObject endObject = new JsonObject();
			endObject.addProperty(FILE_VERSION_KEY, saveObject.fileVersion);

			JsonObject mappingObject = new JsonObject();
			for (Entry<String, String> entry : saveObject.biomeMappings.entrySet()) {
				mappingObject.addProperty(entry.getKey(), entry.getValue());
			}
			endObject.add(BIOME_MAPPINGS, mappingObject);

			JsonObject attributeObject = new JsonObject();
			for (Entry<String, TreeMap<String, BiomeGroupBuilder>> outerEntry : saveObject.configNameToAttributeGroups
					.get().entrySet()) {
				String configName = outerEntry.getKey();
				JsonObject biomeObject = new JsonObject();
				for (Entry<String, BiomeGroupBuilder> innerEntry : outerEntry.getValue().entrySet()) {
					String groupName = innerEntry.getKey();
					BiomeGroupBuilder group = innerEntry.getValue();
					biomeObject.addProperty(groupName, group.content());
				}
				attributeObject.add(configName, biomeObject);
			}
			endObject.add(ATTRIBUTE_GROUPS, attributeObject);

			JsonObject biomeGroupObject = new JsonObject();
			for (Entry<String, TreeMap<String, BiomeGroupBuilder>> outerEntry : saveObject.configNameToBiomeGroups
					.get().entrySet()) {
				String configName = outerEntry.getKey();
				JsonObject biomeObject = new JsonObject();
				for (Entry<String, BiomeGroupBuilder> innerEntry : outerEntry.getValue().entrySet()) {
					String groupName = innerEntry.getKey();
					BiomeGroupBuilder group = innerEntry.getValue();
					biomeObject.addProperty(groupName, group.content());

				}
				biomeGroupObject.add(configName, biomeObject);
			}

			return endObject;
		}

		@Override
		public BiomeSettingsLoader deserialize(JsonElement object, Type type, JsonDeserializationContext context)
				throws JsonParseException {
			BiomeSettingsLoader saveObject = new BiomeSettingsLoader();
			JsonObject endObject = object.getAsJsonObject();
			String fileVersion = GsonHelper.getMemberOrDefault(endObject, FILE_VERSION_KEY, FILE_VERSION);

			JsonObject mappingsObject = GsonHelper.getMemberOrDefault(endObject, BIOME_MAPPINGS, new JsonObject());
			for (Entry<String, JsonElement> entry : mappingsObject.entrySet()) {
				saveObject.biomeMappings.put(entry.getKey(), entry.getValue().getAsString());
			}

			JsonElement attrElement = endObject.get(ATTRIBUTE_GROUPS);
			if (attrElement != null && attrElement.isJsonObject()) {
				saveObject.configNameToAttributeGroups = Optional
						.of(new TreeMap<String, TreeMap<String, BiomeGroupBuilder>>());
				JsonObject attributeObject = attrElement.getAsJsonObject();
				for (Entry<String, JsonElement> outerEntry : attributeObject.entrySet()) {
					String configName = outerEntry.getKey();
					TreeMap<String, BiomeGroupBuilder> groupNameToBiomeGroup = saveObject.configNameToAttributeGroups
							.get().get(configName);
					if (groupNameToBiomeGroup == null) {
						groupNameToBiomeGroup = new TreeMap<String, BiomeGroupBuilder>();
						saveObject.configNameToAttributeGroups.get().put(configName, groupNameToBiomeGroup);
					}
					JsonObject innerObject = outerEntry.getValue().getAsJsonObject();
					for (Entry<String, JsonElement> innerEntry : innerObject.entrySet()) {
						String groupName = innerEntry.getKey();
						String expression = innerEntry.getValue().getAsString();
						groupNameToBiomeGroup.put(groupName, new BiomeGroupBuilder(groupName, configName, expression));
					}
				}
			}
			JsonElement biomeElement = endObject.get(BIOME_GROUPS);
			if (biomeElement != null && biomeElement.isJsonObject()) {
				saveObject.configNameToBiomeGroups = Optional
						.of(new TreeMap<String, TreeMap<String, BiomeGroupBuilder>>());

				JsonObject biomeGroupObject = biomeElement.getAsJsonObject();
				for (Entry<String, JsonElement> outerEntry : biomeGroupObject.entrySet()) {
					String configName = outerEntry.getKey();
					TreeMap<String, BiomeGroupBuilder> groupNameToBiomeGroup = saveObject.configNameToBiomeGroups.get()
							.get(configName);
					if (groupNameToBiomeGroup == null) {
						groupNameToBiomeGroup = new TreeMap<String, BiomeGroupBuilder>();
						saveObject.configNameToBiomeGroups.get().put(configName, groupNameToBiomeGroup);
					}
					JsonObject innerObject = outerEntry.getValue().getAsJsonObject();
					for (Entry<String, JsonElement> innerEntry : innerObject.entrySet()) {
						String groupName = innerEntry.getKey();
						String expression = innerEntry.getValue().getAsString();
						groupNameToBiomeGroup.put(groupName, new BiomeGroupBuilder(groupName, configName, expression));
					}
				}
			}
			saveObject.fileVersion = fileVersion;
			return saveObject;
		}
	}
}
