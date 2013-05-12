package jas.common.spawner.creature.handler.parsing.keys;

import jas.common.JASLog;
import jas.common.spawner.creature.handler.parsing.ParsingHelper;
import jas.common.spawner.creature.handler.parsing.TypeValuePair;
import jas.common.spawner.creature.handler.parsing.settings.OptionalSettings.Operand;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.world.World;

public class KeyParserOpaqueBlock extends KeyParserBase {

    public KeyParserOpaqueBlock(Key key) {
        super(key, true, KeyType.CHAINABLE);
    }

    @Override
    public boolean parseChainable(String[] parseable, ArrayList<TypeValuePair> parsedChainable,
            ArrayList<Operand> operandvalue) {
        Operand operand = getOperand(parseable);

        boolean isInverted = false;
        if (parseable[0].startsWith("!")) {
            isInverted = true;
        }

        TypeValuePair typeValue = null;

        if (parseable.length == 4 || parseable.length == 7) {
            int rangeX = ParsingHelper.parseInteger(parseable[1], 0, "opaqueRangeX");
            int rangeY = ParsingHelper.parseInteger(parseable[2], 0, "opaqueRangeY");
            int rangeZ = ParsingHelper.parseInteger(parseable[3], 0, "opaqueRangeZ");
            if (parseable.length == 7) {
                int offsetX = ParsingHelper.parseInteger(parseable[4], 0, "opaqueOffsetX");
                int offsetY = ParsingHelper.parseInteger(parseable[5], 0, "opaqueOffsetY");
                int offsetZ = ParsingHelper.parseInteger(parseable[6], 0, "opaqueOffsetZ");
                typeValue = new TypeValuePair(key, new Object[] { isInverted, rangeX, rangeY, rangeZ, offsetX, offsetY,
                        offsetZ });
            } else {
                typeValue = new TypeValuePair(key, new Object[] { isInverted, rangeX, rangeY, rangeZ });
            }
        } else if (parseable.length == 2 || parseable.length == 5) {
            int range = ParsingHelper.parseInteger(parseable[1], 0, "opaqueRange");
            if (parseable.length == 5) {
                int offsetX = ParsingHelper.parseInteger(parseable[2], 0, "opaqueOffsetX");
                int offsetY = ParsingHelper.parseInteger(parseable[3], 0, "opaqueOffsetY");
                int offsetZ = ParsingHelper.parseInteger(parseable[4], 0, "opaqueOffsetZ");
                typeValue = new TypeValuePair(key, new Object[] { isInverted, range, offsetX, offsetY, offsetZ });
            } else {
                typeValue = new TypeValuePair(key, new Object[] { isInverted, range });
            }
        } else {
            JASLog.severe("Error Parsing %s Block Parameter. Invalid Argument Length of %s.", key.key, parseable.length);
        }

        if (typeValue != null && typeValue.getValue() != null) {
            parsedChainable.add(typeValue);
            operandvalue.add(operand);
            return true;
        }
        return false;
    }

    @Override
    public boolean parseValue(String[] parseable, HashMap<String, Object> valueCache) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isValidLocation(World world, int xCoord, int yCoord, int zCoord, TypeValuePair typeValuePair,
            HashMap<String, Object> valueCache) {

        Object[] values = (Object[]) typeValuePair.getValue();
        boolean isInverted = (Boolean) values[0];
        int offsetX = 0;
        int offsetY = 0;
        int offsetZ = 0;
        int rangeX = 0;
        int rangeY = 0;
        int rangeZ = 0;

        if (values.length == 4 || values.length == 7) {
            rangeX = (Integer) values[1];
            rangeY = (Integer) values[2];
            rangeZ = (Integer) values[3];
            if (values.length == 7) {
                offsetX = (Integer) values[4];
                offsetY = (Integer) values[5];
                offsetZ = (Integer) values[6];
            }
        } else if (values.length == 2 || values.length == 5) {
            rangeX = (Integer) values[1];
            rangeY = rangeX;
            rangeZ = rangeX;
            if (values.length == 5) {
                offsetX = (Integer) values[2];
                offsetY = (Integer) values[3];
                offsetZ = (Integer) values[4];
            }
        }

        for (int i = -rangeX; i <= rangeX; i++) {
            for (int k = -rangeZ; k <= rangeZ; k++) {
                for (int j = rangeY; j <= rangeY; j++) {
                    boolean isOpaque = world.isBlockOpaqueCube(xCoord + offsetX + i, yCoord + offsetY + j, zCoord
                            + offsetZ + k);
                    if (!isInverted && isOpaque || isInverted && !isOpaque) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}