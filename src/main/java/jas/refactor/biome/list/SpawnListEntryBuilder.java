package jas.refactor.biome.list;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.util.ChunkCoordinates;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import jas.common.spawner.creature.entry.SpawnListEntry;
import jas.common.spawner.creature.handler.parsing.settings.OptionalSettings.Operand;
import jas.refactor.entities.Group;
import jas.refactor.entities.Group.MutableGroup;
import jas.refactor.mvel.LinkedMVELExpression;
import jas.refactor.mvel.MVELExpression;

public class SpawnListEntryBuilder implements MutableGroup {
	private transient Set<String> mappings;
	/** String Used to Build Mappings i.e. {desert,A|Forest,glacier} */
	private List<String> contents;
	private String spawnListEntryID;

	private String livingHandlerID;
	private String livingTypeID;

	private String weight;
	private String passivePackSize;
	private String chunkPackSize; // [int] Replaces old min/max: returns the random result itself
	private String canSpawn;
	private String postSpawn;
	private String entityToSpawn;

	public SpawnListEntryBuilder() {
		this.setLivingHandlerID(null);
		this.setLivingTypeID(null);
		this.setWeight("0");
		this.setPassivePackSize("3");
		this.setChunkPackSize("0 + util.rand(1 + 4 - 0)");

		this.setCanSpawn("");
		this.setPostSpawn("");
		this.setEntityToSpawn("");
		this.setContents(new ArrayList<String>());
		this.setResults(new HashSet<String>());
		this.recalculateEntryID();
	}

	public SpawnListEntryBuilder(String livingHandlerID, String livingTypeID, String biomeExpression) {
		this.setLivingHandlerID(livingHandlerID);
		this.setLivingTypeID(livingTypeID);
		this.setWeight("0");
		this.setPassivePackSize("3");
		this.setChunkPackSize("0 + util.rand(1 + 4 - 0)");

		this.setCanSpawn("");
		this.setPostSpawn("");
		this.setEntityToSpawn("");

		this.setContents(Arrays.asList(biomeExpression.split("?=[&+-/]")));
		this.setResults(new HashSet<String>());
		this.recalculateEntryID();
	}

	public SpawnListEntryBuilder(SpawnListEntry entry) {
		this.spawnListEntryID = entry.spawnListEntryID;
		this.setLivingHandlerID(entry.livingHandlerID);
		this.setLivingTypeID(entry.livingTypeID);
		this.setWeight(entry.weight.expression);
		this.setPassivePackSize(entry.passivePackSize.expression);
		this.setChunkPackSize(entry.chunkPackSize.expression);

		this.setCanSpawn(entry.canSpawn.expression);
		this.setPostSpawn(entry.postSpawn.expression);
		this.setEntityToSpawn(entry.entityToSpawn.expression);
		this.setContents(entry.contents);
		this.setResults(entry.mappings);
		this.recalculateEntryID();
	}

	private void recalculateEntryID() {
		spawnListEntryID = livingHandlerID + livingTypeID + contents.toString();
	}

	public static class SpawnListEntry implements Group {
		public final String spawnListEntryID;
		private final transient ImmutableSet<String> mappings;
		/** String Used to Build Mappings i.e. {desert,A|Forest,glacier} */
		private final transient ImmutableList<String> contents;

		public final String livingHandlerID;
		public final String livingTypeID;

		public final MVELExpression<Integer> weight;
		public final MVELExpression<Integer> passivePackSize;
		public final MVELExpression<Integer> chunkPackSize;
		public final MVELExpression<Boolean> canSpawn;
		public final MVELExpression<Boolean> postSpawn;
		public final MVELExpression<EntitySpawn> entityToSpawn;

		private SpawnListEntry(SpawnListEntryBuilder builder) {
			this.spawnListEntryID = builder.spawnListEntryID;
			this.mappings = ImmutableSet.<String> builder().addAll(builder.results()).build();
			this.contents = ImmutableList.<String> builder().addAll(builder.contents()).build();
			this.livingHandlerID = builder.spawnListEntryID;
			this.livingTypeID = builder.spawnListEntryID;
			this.weight = new MVELExpression<Integer>(builder.getWeight());
			this.passivePackSize = new MVELExpression<Integer>(builder.getPassivePackSize());
			this.chunkPackSize = new MVELExpression<Integer>(builder.getChunkPackSize());
			this.canSpawn = new MVELExpression<Boolean>(builder.getCanSpawn());
			this.postSpawn = new MVELExpression<Boolean>(builder.getPostSpawn());
			this.entityToSpawn = new MVELExpression<EntitySpawn>(builder.getEntityToSpawn());
		}

		@Override
		public String iD() {
			return spawnListEntryID;
		}

		@Override
		public Set<String> results() {
			return mappings;
		}

		@Override
		public List<String> contents() {
			return contents;
		}
	}

	public SpawnListEntry build() {
		return new SpawnListEntry(this);
	}

	@Override
	public String iD() {
		return spawnListEntryID;
	}

	public String getLivingHandlerID() {
		return livingHandlerID;
	}

	public void setLivingHandlerID(String livingHandlerID) {
		this.livingHandlerID = livingHandlerID;
		recalculateEntryID();
	}

	public String getLivingTypeID() {
		return livingTypeID;
	}

	public void setLivingTypeID(String livingTypeID) {
		this.livingTypeID = livingTypeID;
		recalculateEntryID();
	}

	public String getWeight() {
		return weight;
	}

	public void setWeight(String weight) {
		this.weight = weight;
	}

	public String getPassivePackSize() {
		return passivePackSize;
	}

	public void setPassivePackSize(String passivePackSize) {
		this.passivePackSize = passivePackSize;
	}

	public String getChunkPackSize() {
		return chunkPackSize;
	}

	public void setChunkPackSize(String chunkPackSize) {
		this.chunkPackSize = chunkPackSize;
	}

	public String getCanSpawn() {
		return canSpawn;
	}

	public void setCanSpawn(String canSpawn) {
		this.canSpawn = canSpawn;
	}

	public String getPostSpawn() {
		return postSpawn;
	}

	public void setPostSpawn(String postSpawn) {
		this.postSpawn = postSpawn;
	}

	public String getEntityToSpawn() {
		return entityToSpawn;
	}

	public void setEntityToSpawn(String entityToSpawn) {
		this.entityToSpawn = entityToSpawn;
	}

	@Override
	public Set<String> results() {
		return mappings;
	}

	@Override
	public List<String> contents() {
		return contents;
	}

	@Override
	public void setResults(Set<String> results) {
		this.mappings = new HashSet<String>(results);
	}

	@Override
	public void setContents(List<String> contents) {
		this.contents = new ArrayList<String>(contents);
		recalculateEntryID();
	}
}
