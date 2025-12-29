package programs;

import com.battle.heroes.army.Army;
import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.Program;
import com.battle.heroes.util.GameSpeedUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class SimulateBattleImplTest {

    private SimulateBattleImpl simulator;

    @BeforeEach
    void setUp() {
        simulator = new SimulateBattleImpl();
    }

    private Unit createUnit(String name, int hp, int atk) {
        Unit unit = new Unit(
                name,
                name,
                hp,
                atk,
                0,
                "MELEE",
                new java.util.HashMap<>(),
                new java.util.HashMap<>(),
                0, 0
        );
        unit.setAlive(true);
        return unit;
    }

    private Army createArmy(List<Unit> units) {
        Army army = new Army();
        army.setUnits(units);
        return army;
    }

    private void assignPrograms(Army ally, Army enemy) {
        for (Unit unit : ally.getUnits()) {
            unit.setProgram(new TestProgram(unit, ally, enemy));
        }
    }

    @Test
    void battleShouldEndWhenOneArmyIsDestroyed() throws InterruptedException {
        List<Unit> playerUnits = List.of(createUnit("P1", 30, 10));
        List<Unit> computerUnits = List.of(createUnit("C1", 20, 5));

        Army playerArmy = createArmy(new ArrayList<>(playerUnits));
        Army computerArmy = createArmy(new ArrayList<>(computerUnits));

        assignPrograms(playerArmy, computerArmy);
        assignPrograms(computerArmy, playerArmy);

        simulator.simulate(playerArmy, computerArmy);

        assertTrue(
                playerArmy.getUnits().stream().anyMatch(Unit::isAlive) ^
                        computerArmy.getUnits().stream().anyMatch(Unit::isAlive),
                "После боя должна остаться только одна живая армия"
        );
    }

    @Test
    void unitsShouldAttackAndDealDamage() throws InterruptedException {
        Unit p = createUnit("Player", 50, 20);
        Unit c = createUnit("Computer", 50, 10);

        Army playerArmy = createArmy(List.of(p));
        Army computerArmy = createArmy(List.of(c));

        assignPrograms(playerArmy, computerArmy);
        assignPrograms(computerArmy, playerArmy);

        simulator.simulate(playerArmy, computerArmy);

        assertTrue(
                !p.isAlive() || !c.isAlive(),
                "Хотя бы один юнит должен погибнуть"
        );
    }

    @Test
    void battleShouldHandleDifferentArmySizes() throws InterruptedException {
        List<Unit> playerUnits = List.of(
                createUnit("P1", 40, 10),
                createUnit("P2", 40, 10)
        );

        List<Unit> computerUnits = List.of(
                createUnit("C1", 30, 15)
        );

        Army playerArmy = createArmy(playerUnits);
        Army computerArmy = createArmy(computerUnits);

        assignPrograms(playerArmy, computerArmy);
        assignPrograms(computerArmy, playerArmy);

        simulator.simulate(playerArmy, computerArmy);

        assertTrue(
                playerArmy.getUnits().stream().anyMatch(Unit::isAlive) ||
                        computerArmy.getUnits().stream().anyMatch(Unit::isAlive),
                "Бой должен корректно завершиться при разных размерах армий"
        );
    }

    @Test
    void unitWithoutTargetShouldNotBreakSimulation() throws InterruptedException {
        Unit p = createUnit("Player", 10, 5);

        Army playerArmy = createArmy(List.of(p));
        Army computerArmy = createArmy(new ArrayList<>());

        p.setProgram(new TestProgram(p, playerArmy, computerArmy));

        simulator.simulate(playerArmy, computerArmy);

        assertTrue(p.isAlive(), "Юнит не должен погибнуть без противников");
    }

    @Test
    void simulateShouldNotRunInfinitely() throws InterruptedException {
        Unit p = createUnit("P", 10, 100);
        Unit c = createUnit("C", 10, 100);

        Army playerArmy = createArmy(List.of(p));
        Army computerArmy = createArmy(List.of(c));

        assignPrograms(playerArmy, computerArmy);
        assignPrograms(computerArmy, playerArmy);

        simulator.simulate(playerArmy, computerArmy);

        assertFalse(
                p.isAlive() && c.isAlive(),
                "Метод simulate не должен зацикливаться"
        );
    }

    @Test
    void simultaneousDeathShouldBeHandledCorrectly() throws InterruptedException {
        Unit p1 = createUnit("P1", 10, 50);
        Unit p2 = createUnit("P2", 10, 50);
        Unit c1 = createUnit("C1", 20, 25);
        Unit c2 = createUnit("C2", 20, 25);

        Army playerArmy = createArmy(List.of(p1, p2));
        Army computerArmy = createArmy(List.of(c1, c2));

        assignPrograms(playerArmy, computerArmy);
        assignPrograms(computerArmy, playerArmy);

        simulator.simulate(playerArmy, computerArmy);

        assertTrue(
                playerArmy.getUnits().stream().anyMatch(Unit::isAlive) ^
                        computerArmy.getUnits().stream().anyMatch(Unit::isAlive),
                "После боя должна остаться только одна армия"
        );
    }

    @Test
    void largeArmiesSimulation() throws InterruptedException {
        List<Unit> playerUnits = new ArrayList<>();
        List<Unit> computerUnits = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            playerUnits.add(createUnit("P" + i, 50, 10));
            computerUnits.add(createUnit("C" + i, 50, 10));
        }
        Army playerArmy = createArmy(playerUnits);
        Army computerArmy = createArmy(computerUnits);

        assignPrograms(playerArmy, computerArmy);
        assignPrograms(computerArmy, playerArmy);

        simulator.simulate(playerArmy, computerArmy);

        assertFalse(
                playerArmy.getUnits().stream().allMatch(Unit::isAlive) &&
                        computerArmy.getUnits().stream().allMatch(Unit::isAlive),
                "Должна пройти хотя бы одна атака и у кого-то должно измениться здоровье"
        );
    }


    private static class TestProgram extends Program {

        public TestProgram(Unit unit, Army allyArmy, Army enemyArmy) {
            super(unit, allyArmy, enemyArmy, new GameSpeedUtil(5));
        }

        @Override
        public Unit attack() {
            for (Unit enemy : enemyArmy.getUnits()) {
                if (enemy != null && enemy.isAlive()) {
                    enemy.setHealth(enemy.getHealth() - unit.getBaseAttack());
                    if (enemy.getHealth() <= 0) {
                        enemy.setAlive(false);
                    }
                    return enemy;
                }
            }
            return null;
        }
    }
}


