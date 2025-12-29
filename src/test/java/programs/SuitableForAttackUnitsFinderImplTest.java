package programs;

import com.battle.heroes.army.Unit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SuitableForAttackUnitsFinderImplTest {

    private SuitableForAttackUnitsFinderImpl finder;
    private List<List<Unit>> unitsByRow;

    @BeforeEach
    void setUp() {
        finder = new SuitableForAttackUnitsFinderImpl();
        unitsByRow = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            unitsByRow.add(new ArrayList<>());
        }
    }

    private Unit createUnit(String type, int x, int y) {
        Unit unit = new Unit(
                type,
                type,
                100,
                10,
                50,
                "MELEE",
                new HashMap<>(),
                new HashMap<>(),
                x, y
        );
        unit.setAlive(true);
        return unit;
    }

    @Test
    void shouldReturnEmptyListWhenInputIsNull() {
        List<Unit> result = finder.getSuitableUnits(null, true);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyListWhenInputIsEmpty() {
        List<List<Unit>> emptyList = new ArrayList<>();
        List<Unit> result = finder.getSuitableUnits(emptyList, true);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnCorrectUnitsForLeftArmyTarget() {
        List<Unit> row0 = unitsByRow.get(0);
        row0.add(createUnit("SWORDSMAN", 0, 0)); // подходит
        row0.add(createUnit("ARCHER", 0, 1));
        row0.add(createUnit("ARCHER", 0, 2));
        row0.add(createUnit("PIKEMAN", 0, 3));
        row0.add(createUnit("ARCHER", 0, 4));

        List<Unit> row1 = unitsByRow.get(1);


        List<Unit> row2 = unitsByRow.get(2);
        row2.add(null);
        row2.add(createUnit("KNIGHT", 2, 1)); //  подходит
        row2.add(null);
        row2.add(createUnit("ARCHER", 2, 3)); //  подходит
        row2.add(null);

        List<Unit> result = finder.getSuitableUnits(unitsByRow, true);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("SWORDSMAN", result.get(0).getUnitType());
        assertEquals("KNIGHT", result.get(1).getUnitType());
        assertEquals("ARCHER", result.get(2).getUnitType());
    }

    @Test
    void shouldReturnCorrectUnitsForRightArmyTarget() {
        List<Unit> row0 = unitsByRow.get(0);
        row0.add(createUnit("SWORDSMAN", 25, 0));
        row0.add(createUnit("ARCHER", 25, 1)); // подходит
        row0.add(null);
        row0.add(createUnit("PIKEMAN", 25, 3));
        row0.add(createUnit("SWORDSMAN", 25, 4));// подходит

        List<Unit> row1 = unitsByRow.get(1);
        row1.add(createUnit("KNIGHT", 26, 0));  // подходит
        row1.add(null);
        row1.add(null);

        List<Unit> row2 = unitsByRow.get(2);

        List<Unit> result = finder.getSuitableUnits(unitsByRow, false);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("ARCHER", result.get(0).getUnitType());
        assertEquals("SWORDSMAN", result.get(1).getUnitType());
        assertEquals("KNIGHT", result.get(2).getUnitType());
    }

    @Test
    void shouldIgnoreDeadUnits() {
        List<Unit> row0 = unitsByRow.get(0);
        Unit alive1 = createUnit("SWORDSMAN", 0, 0);   // Живой
        Unit dead = createUnit("ARCHER", 0, 1);        // Мертвый
        Unit alive2 = createUnit("PIKEMAN", 0, 2);     // Живой

        dead.setAlive(false);

        row0.add(alive1);
        row0.add(dead);
        row0.add(alive2);

        List<Unit> resultLeft = finder.getSuitableUnits(unitsByRow, true);

        assertEquals(2, resultLeft.size());
        assertEquals(alive1, resultLeft.get(0));
        assertEquals(alive2, resultLeft.get(1));

        List<Unit> resultRight = finder.getSuitableUnits(unitsByRow, false);

        assertEquals(2, resultRight.size());
        assertEquals(alive1, resultRight.get(0));
        assertEquals(alive2, resultRight.get(1));
    }

    @Test
    void shouldHandleAllEmptyRows() {

        List<Unit> row0 = unitsByRow.get(0);
        row0.add(null);
        row0.add(null);

        List<Unit> result = finder.getSuitableUnits(unitsByRow, true);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldHandleRowsWithOnlyNulls() {

        List<Unit> row0 = unitsByRow.get(0);
        row0.add(null);
        row0.add(null);
        row0.add(null);

        List<Unit> row1 = unitsByRow.get(1);
        row1.add(null);

        List<Unit> row2 = unitsByRow.get(2);
        row2.add(null);
        row2.add(null);
        row2.add(null);
        row2.add(null);

        List<Unit> result = finder.getSuitableUnits(unitsByRow, false);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldWorkWithLessThanThreeRows() {

        List<List<Unit>> twoRows = new ArrayList<>();
        twoRows.add(new ArrayList<>());
        twoRows.add(new ArrayList<>());


        twoRows.get(0).add(createUnit("SWORDSMAN", 0, 0));
        twoRows.get(0).add(createUnit("ARCHER", 0, 1));

        List<Unit> result = finder.getSuitableUnits(twoRows, true);

        assertEquals(1, result.size());
        assertEquals("SWORDSMAN", result.get(0).getUnitType());
    }

    @Test
    void shouldHandleSingleUnitInRow() {
        List<Unit> row0 = unitsByRow.get(0);
        Unit singleUnit = createUnit("KNIGHT", 0, 0);
        row0.add(singleUnit);

        List<Unit> resultLeft = finder.getSuitableUnits(unitsByRow, true);

        assertEquals(1, resultLeft.size());
        assertEquals(singleUnit, resultLeft.get(0));

        List<Unit> resultRight = finder.getSuitableUnits(unitsByRow, false);

        assertEquals(1, resultRight.size());
        assertEquals(singleUnit, resultRight.get(0));
    }

    @Test
    void shouldHandleConsecutiveUnits() {
        List<Unit> row0 = unitsByRow.get(0);
        Unit unit1 = createUnit("SWORDSMAN", 0, 0);
        Unit unit2 = createUnit("ARCHER", 0, 1);
        Unit unit3 = createUnit("PIKEMAN", 0, 2);
        Unit unit4 = createUnit("KNIGHT", 0, 3);

        row0.add(unit1);
        row0.add(unit2);
        row0.add(unit3);
        row0.add(unit4);

        List<Unit> resultLeft = finder.getSuitableUnits(unitsByRow, true);

        assertEquals(1, resultLeft.size());
        assertEquals(unit1, resultLeft.get(0));

        List<Unit> resultRight = finder.getSuitableUnits(unitsByRow, false);

        assertEquals(1, resultRight.size());
        assertEquals(unit4, resultRight.get(0));
    }

    @Test
    void shouldHandleMixedNullAndUnits() {
        List<Unit> row0 = unitsByRow.get(0);
        row0.add(null);
        Unit unit1 = createUnit("SWORDSMAN", 0, 1);
        row0.add(unit1);
        row0.add(null);
        Unit unit2 = createUnit("ARCHER", 0, 3);
        row0.add(unit2);
        row0.add(null);
        Unit unit3 = createUnit("PIKEMAN", 0, 5);
        row0.add(unit3);
        row0.add(null);


        List<Unit> resultLeft = finder.getSuitableUnits(unitsByRow, true);

        assertEquals(3, resultLeft.size());
        assertEquals(unit1, resultLeft.get(0));
        assertEquals(unit2, resultLeft.get(1));
        assertEquals(unit3, resultLeft.get(2));

        List<Unit> resultRight = finder.getSuitableUnits(unitsByRow, false);

        assertEquals(3, resultRight.size());
        assertEquals(unit1, resultRight.get(0));
        assertEquals(unit2, resultRight.get(1));
        assertEquals(unit3, resultRight.get(2));
    }
}