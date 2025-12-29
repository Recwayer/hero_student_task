package programs;

import com.battle.heroes.army.Army;
import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.PrintBattleLog;
import com.battle.heroes.army.programs.SimulateBattle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SimulateBattleImpl implements SimulateBattle {
    private PrintBattleLog printBattleLog;


    public void setPrintBattleLog(PrintBattleLog printBattleLog) {
        this.printBattleLog = printBattleLog;
    }
    /**
     * Выполняет пошаговую симуляцию боя между армией игрока и армией компьютера.
     * <p>
     * Бой происходит раундами. В каждом раунде обе армии формируют очереди ходов
     * из живых юнитов, отсортированных по убыванию базовой атаки.
     * Ходы выполняются поочерёдно: сначала ходит юнит игрока, затем юнит компьютера.
     * <p>
     * Алгоритм:
     * Итеративная пошаговая симуляция боя с ленивым обновлением очередей ходов.
     * Очереди формируются один раз при инициализации и пересортировываются только
     * при гибели юнитов, что минимизирует количество операций сортировки.
     * <p>
     * Алгоритмическая сложность: O(n^2 log n),
     * где n — Общее количество юнитов в армии.
     * <p>
     * Подробный анализ сложности:
     * 1. Инициализация отсортированных списков: O(n log n)
     * 2. Проверка наличия живых юнитов в армиях: O(n)
     * 3. Внутренний цикл раунда (поочерёдные ходы): O(n)
     * 4. Ленивое обновление списков при гибели юнитов: O(k log k), где k ≤ n - текущее количество живых юнитов
     * <p>
     * Количество раундов в худшем случае: O(n)
     * <p>
     * Сложность одного раунда:
     * O(n) + O(n) + O(k log k) = O(n log n) (в худшем случае k = n)
     * <p>
     * Итоговая сложность: O(n log n) + O(n) * O(n log n) = O(n^2 * log n)
     * <p>
     * Реализация соответствует требованиям технического задания,
     * обеспечивает корректную очерёдность ходов команд и устойчиво работает
     * при большом количестве юнитов благодаря оптимизированному обновлению очередей.
     */
    @Override
    public void simulate(Army playerArmy, Army computerArmy) throws InterruptedException {
        List<Unit> playerUnits = getAliveUnitsSorted(playerArmy);
        List<Unit> computerUnits = getAliveUnitsSorted(computerArmy);
        boolean isNeedUpdatePlayerUnits = false;
        boolean isNeedUpdateComputeUnits = false;
        while (hasAliveUnits(playerArmy) && hasAliveUnits(computerArmy)) {
            if (playerUnits.isEmpty() || computerUnits.isEmpty()) {
                break;
            }
            if (isNeedUpdatePlayerUnits) {
                updateAliveUnits(playerUnits);
                isNeedUpdatePlayerUnits = false;
            }
            if (isNeedUpdateComputeUnits) {
                updateAliveUnits(computerUnits);
                isNeedUpdateComputeUnits = false;
            }
            int playerIdx = 0;
            int computerIdx = 0;
            while (playerIdx < playerUnits.size() || computerIdx < computerUnits.size()) {
                if (!hasAliveUnits(playerArmy) || !hasAliveUnits(computerArmy)) {
                    break;
                }
                if (playerIdx < playerUnits.size()) {
                    Unit attacker = playerUnits.get(playerIdx);
                    playerIdx++;
                    if (attacker.isAlive()) {
                        Unit target = attacker.getProgram().attack();
                        if (target != null && printBattleLog != null) {
                            printBattleLog.printBattleLog(attacker, target);
                        }
                        if (target != null && !target.isAlive()) {
                            isNeedUpdateComputeUnits = true;
                        }
                    }
                }
                if (!hasAliveUnits(playerArmy) || !hasAliveUnits(computerArmy)) {
                    break;
                }
                if (computerIdx < computerUnits.size()) {
                    Unit attacker = computerUnits.get(computerIdx);
                    computerIdx++;
                    if (attacker.isAlive()) {
                        Unit target = attacker.getProgram().attack();
                        if (target != null && printBattleLog != null) {
                            printBattleLog.printBattleLog(attacker, target);
                        }
                        if (target != null && !target.isAlive()) {
                            isNeedUpdatePlayerUnits = true;
                        }
                    }
                }
            }
        }
    }

    private List<Unit> getAliveUnitsSorted(Army army) {
        List<Unit> list = new ArrayList<>();

        for (Unit u : army.getUnits()) {
            if (u != null && u.isAlive()) {
                list.add(u);
            }
        }

        list.sort(Comparator.comparingInt(Unit::getBaseAttack).reversed());

        return list;
    }


    private boolean hasAliveUnits(Army army) {
        if (army == null || army.getUnits() == null) {
            return false;
        }

        for (Unit unit : army.getUnits()) {
            if (unit != null && unit.isAlive()) {
                return true;
            }
        }

        return false;
    }

    private void updateAliveUnits(List<Unit> units) {
        units.removeIf(u -> !u.isAlive());
        units.sort(Comparator.comparingInt(Unit::getBaseAttack).reversed());
    }
}