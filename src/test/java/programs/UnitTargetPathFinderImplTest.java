package programs;

import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.Edge;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UnitTargetPathFinderImplTest {

    private UnitTargetPathFinderImpl finder;

    @BeforeEach
    void setUp() {
        finder = new UnitTargetPathFinderImpl();
    }

    private Unit createUnit(String type, int x, int y, boolean alive) {
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
        unit.setAlive(alive);
        return unit;
    }

    @Test
    void shouldReturnEmptyListWhenAttackUnitIsNull() {
        Unit targetUnit = createUnit("ARCHER", 5, 5, true);
        List<Unit> obstacles = new ArrayList<>();

        List<Edge> result = finder.getTargetPath(null, targetUnit, obstacles);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyListWhenTargetUnitIsNull() {
        Unit attackUnit = createUnit("SWORDSMAN", 0, 0, true);
        List<Unit> obstacles = new ArrayList<>();

        List<Edge> result = finder.getTargetPath(attackUnit, null, obstacles);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyListWhenObstaclesListIsNull() {
        Unit attackUnit = createUnit("SWORDSMAN", 0, 0, true);
        Unit targetUnit = createUnit("ARCHER", 5, 5, true);

        List<Edge> result = finder.getTargetPath(attackUnit, targetUnit, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnSinglePointPathWhenUnitsAreAtSamePosition() {
        Unit attackUnit = createUnit("SWORDSMAN", 10, 10, true);
        Unit targetUnit = createUnit("ARCHER", 10, 10, true);
        List<Unit> obstacles = new ArrayList<>();

        List<Edge> result = finder.getTargetPath(attackUnit, targetUnit, obstacles);

        assertEquals(1, result.size());
        assertEquals(10, result.get(0).getX());
        assertEquals(10, result.get(0).getY());
    }

    @Test
    void shouldFindDirectPathWhenNoObstacles() {
        Unit attackUnit = createUnit("SWORDSMAN", 0, 0, true);
        Unit targetUnit = createUnit("ARCHER", 3, 3, true);
        List<Unit> obstacles = new ArrayList<>();

        List<Edge> result = finder.getTargetPath(attackUnit, targetUnit, obstacles);

        assertFalse(result.isEmpty());
        assertEquals(0, result.get(0).getX());
        assertEquals(0, result.get(0).getY());
        assertEquals(3, result.get(result.size() - 1).getX());
        assertEquals(3, result.get(result.size() - 1).getY());

        for (int i = 0; i < result.size() - 1; i++) {
            Edge current = result.get(i);
            Edge next = result.get(i + 1);
            int dx = Math.abs(current.getX() - next.getX());
            int dy = Math.abs(current.getY() - next.getY());
            assertTrue(dx <= 1 && dy <= 1, "Шаг должен быть не более 1 клетки в любом направлении");
        }
    }

    @Test
    void shouldFindPathAroundObstacles() {
        Unit attackUnit = createUnit("SWORDSMAN", 0, 0, true);
        Unit targetUnit = createUnit("ARCHER", 4, 0, true);


        List<Unit> obstacles = new ArrayList<>();
        obstacles.add(createUnit("WALL", 1, 0, true));
        obstacles.add(createUnit("WALL", 2, 0, true));
        obstacles.add(createUnit("WALL", 3, 0, true));

        List<Edge> result = finder.getTargetPath(attackUnit, targetUnit, obstacles);

        assertFalse(result.isEmpty());
        assertEquals(0, result.get(0).getX());
        assertEquals(0, result.get(0).getY());
        assertEquals(4, result.get(result.size() - 1).getX());
        assertEquals(0, result.get(result.size() - 1).getY());

        for (Edge edge : result) {
            assertFalse(edge.getX() == 1 && edge.getY() == 0);
            assertFalse(edge.getX() == 2 && edge.getY() == 0);
            assertFalse(edge.getX() == 3 && edge.getY() == 0);
        }
    }

    @Test
    void shouldReturnEmptyListWhenPathIsBlocked() {
        Unit attackUnit = createUnit("SWORDSMAN", 0, 0, true);
        Unit targetUnit = createUnit("ARCHER", 2, 0, true);

        List<Unit> obstacles = new ArrayList<>();
        obstacles.add(createUnit("WALL", 1, 0, true));
        obstacles.add(createUnit("WALL", 2, 1, true));
        obstacles.add(createUnit("WALL", 1, 1, true));
        obstacles.add(createUnit("WALL", 0, 1, true));

        List<Edge> result = finder.getTargetPath(attackUnit, targetUnit, obstacles);

        assertTrue(result.isEmpty(), "Должен вернуть пустой список при отсутствии пути");
    }

    @Test
    void shouldIgnoreDeadUnitsAsObstacles() {
        Unit attackUnit = createUnit("SWORDSMAN", 0, 0, true);
        Unit targetUnit = createUnit("ARCHER", 2, 0, true);

        List<Unit> obstacles = new ArrayList<>();
        Unit deadUnit = createUnit("ZOMBIE", 1, 0, false);
        obstacles.add(deadUnit);

        List<Edge> result = finder.getTargetPath(attackUnit, targetUnit, obstacles);

        assertFalse(result.isEmpty());
        boolean passesThroughDeadUnit = result.stream()
                .anyMatch(edge -> edge.getX() == 1 && edge.getY() == 0);
        assertTrue(passesThroughDeadUnit, "Должен проходить через клетку с мертвым юнитом");
    }

    @Test
    void shouldFindDiagonalPath() {
        Unit attackUnit = createUnit("SWORDSMAN", 0, 0, true);
        Unit targetUnit = createUnit("ARCHER", 3, 3, true);
        List<Unit> obstacles = new ArrayList<>();

        List<Edge> result = finder.getTargetPath(attackUnit, targetUnit, obstacles);

        assertFalse(result.isEmpty());
        assertEquals(3 + 1, result.size());
    }

    @Test
    void shouldHandleUnitsOutsideFieldBounds() {
        Unit attackUnit = createUnit("SWORDSMAN", -1, 5, true);
        Unit targetUnit = createUnit("ARCHER", 5, 5, true);
        List<Unit> obstacles = new ArrayList<>();

        List<Edge> result = finder.getTargetPath(attackUnit, targetUnit, obstacles);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindPathWithMultipleObstacles() {
        Unit attackUnit = createUnit("SWORDSMAN", 0, 0, true);
        Unit targetUnit = createUnit("ARCHER", 5, 5, true);

        List<Unit> obstacles = new ArrayList<>();
        for (int x = 1; x < 5; x++) {
            obstacles.add(createUnit("WALL", x, 2, true));
        }
        for (int y = 1; y < 5; y++) {
            obstacles.add(createUnit("WALL", 2, y, true));
        }

        List<Edge> result = finder.getTargetPath(attackUnit, targetUnit, obstacles);

        assertFalse(result.isEmpty());
        assertEquals(0, result.get(0).getX());
        assertEquals(0, result.get(0).getY());
        assertEquals(5, result.get(result.size() - 1).getX());
        assertEquals(5, result.get(result.size() - 1).getY());
    }

    @Test
    void shouldReturnShortestPath() {
        Unit attackUnit = createUnit("SWORDSMAN", 0, 0, true);
        Unit targetUnit = createUnit("ARCHER", 4, 0, true);

        List<Unit> obstacles = new ArrayList<>();
        obstacles.add(createUnit("WALL", 2, 0, true));

        List<Edge> result = finder.getTargetPath(attackUnit, targetUnit, obstacles);


        assertFalse(result.isEmpty());
        assertTrue(result.size() <= 8, "Путь должен быть оптимальным по длине");
    }

    @Test
    void shouldHandleEmptyObstaclesList() {
        Unit attackUnit = createUnit("SWORDSMAN", 0, 0, true);
        Unit targetUnit = createUnit("ARCHER", 3, 3, true);
        List<Unit> obstacles = Collections.emptyList();

        List<Edge> result = finder.getTargetPath(attackUnit, targetUnit, obstacles);

        assertFalse(result.isEmpty());
        assertEquals(0, result.get(0).getX());
        assertEquals(0, result.get(0).getY());
        assertEquals(3, result.get(result.size() - 1).getX());
        assertEquals(3, result.get(result.size() - 1).getY());
    }

    @Test
    void shouldFindPathWhenTargetIsSurroundedButAttackUnitCanReach() {
        Unit attackUnit = createUnit("SWORDSMAN", 0, 0, true);
        Unit targetUnit = createUnit("ARCHER", 2, 2, true);

        List<Unit> obstacles = new ArrayList<>();
        obstacles.add(createUnit("WALL", 1, 2, true));
        obstacles.add(createUnit("WALL", 3, 2, true));
        obstacles.add(createUnit("WALL", 2, 1, true));
        obstacles.add(createUnit("WALL", 2, 3, true));
        List<Edge> result = finder.getTargetPath(attackUnit, targetUnit, obstacles);

        assertFalse(result.isEmpty());
        assertEquals(2, result.get(result.size() - 1).getX());
        assertEquals(2, result.get(result.size() - 1).getY());
    }

    @Test
    void shouldHandleLargeDistance() {
        Unit attackUnit = createUnit("SWORDSMAN", 0, 0, true);
        Unit targetUnit = createUnit("ARCHER", 26, 20, true);
        List<Unit> obstacles = new ArrayList<>();

        List<Edge> result = finder.getTargetPath(attackUnit, targetUnit, obstacles);

        assertFalse(result.isEmpty());
        assertEquals(26, result.get(result.size() - 1).getX());
        assertEquals(20, result.get(result.size() - 1).getY());

        for (int i = 0; i < result.size() - 1; i++) {
            Edge current = result.get(i);
            Edge next = result.get(i + 1);
            assertTrue(Math.abs(current.getX() - next.getX()) <= 1);
            assertTrue(Math.abs(current.getY() - next.getY()) <= 1);
        }
    }
}