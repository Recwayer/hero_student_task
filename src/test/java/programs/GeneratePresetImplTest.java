package programs;

import com.battle.heroes.army.Army;
import com.battle.heroes.army.Unit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneratePresetImplTest {

    private List<Unit> unitList;
    private GeneratePresetImpl generator;

    @BeforeEach
    void setUp() {
        unitList = new ArrayList<>();

        unitList.add(createUnit("ARCHER", 50, 25, 20));
        unitList.add(createUnit("SWORDSMAN", 80, 30, 30));
        unitList.add(createUnit("PIKEMAN", 60, 20, 40));
        unitList.add(createUnit("KNIGHT", 100, 40, 50));

        generator = new GeneratePresetImpl();
    }

    private Unit createUnit(String type, int hp, int atk, int cost) {
        Unit unit = new Unit(
                type,
                type,
                hp,
                atk,
                cost,
                "MELEE",
                new HashMap<>(),
                new HashMap<>(),
                0, 0
        );
        unit.setAlive(true);
        return unit;
    }

    @Test
    void shouldGenerateNonEmptyArmy() {
        Army army = generator.generate(unitList, 1500);

        assertNotNull(army);
        assertNotNull(army.getUnits());
        assertFalse(army.getUnits().isEmpty(), "Армия не должна быть пустой");
    }

    @Test
    void shouldNotExceedMaxPoints() {
        int maxPoints = 1500;
        Army army = generator.generate(unitList, maxPoints);

        assertTrue(
                army.getPoints() <= maxPoints,
                "Очки армии не должны превышать лимит"
        );
    }

    @Test
    void shouldRespectMaxUnitsPerType() {
        Army army = generator.generate(unitList, 1500);

        Map<String, Integer> counter = new HashMap<>();
        for (Unit unit : army.getUnits()) {
            counter.merge(unit.getUnitType(), 1, Integer::sum);
        }

        for (Map.Entry<String, Integer> entry : counter.entrySet()) {
            assertTrue(
                    entry.getValue() <= 11,
                    "Тип " + entry.getKey() + " превышает лимит в 11 юнитов"
            );
        }
    }


    @Test
    void shouldUseOnlyAvailableUnitTypes() {
        Set<String> allowedTypes = new HashSet<>();
        for (Unit unit : unitList) {
            allowedTypes.add(unit.getUnitType());
        }

        Army army = generator.generate(unitList, 1500);

        for (Unit unit : army.getUnits()) {
            assertTrue(
                    allowedTypes.contains(unit.getUnitType()),
                    "Обнаружен недопустимый тип юнита"
            );
        }
    }

    @Test
    void unitsShouldHaveValidPositions() {
        Army army = generator.generate(unitList, 1500);

        for (Unit unit : army.getUnits()) {
            assertTrue(unit.getxCoordinate() >= 0 && unit.getxCoordinate() < 3, "Некорректная координата X");
            assertTrue(unit.getyCoordinate() >= 0 && unit.getyCoordinate() < 21, "Некорректная координата Y");
        }
    }

    @Test
    void allUnitsShouldBeAlive() {
        Army army = generator.generate(unitList, 1500);

        for (Unit unit : army.getUnits()) {
            assertTrue(unit.isAlive(), "Юнит должен быть живым");
        }
    }
}
