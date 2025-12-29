package programs;

import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.SuitableForAttackUnitsFinder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SuitableForAttackUnitsFinderImpl implements SuitableForAttackUnitsFinder {

    /**
     * Определяет список юнитов противника, доступных для атаки в текущий момент.
     * <p>
     * Метод анализирует построение армии противника по рядам и определяет, какие юниты
     * не имеют "прикрытия", что делает их уязвимыми для атаки.
     * <p>
     * Логика определения доступности цели:
     * - Если атакует армия игрока (цель — левая армия компьютера):
     *   Юнит доступен для атаки, если у него НЕТ соседа слева (по координате y-1).
     * - Если атакует армия компьютера (цель — правая армия игрока):
     *   Юнит доступен для атаки, если у него НЕТ соседа справа (по координате y+1).
     * На игровом поле координата Y увеличивается вниз по вертикали.
     * <p>
     * Алгоритм:
     * 1. Для каждого ряда армии противника создаётся множество координат живых юнитов.
     * 2. Для каждого живого юнита проверяется наличие соседа в нужном направлении.
     * 3. Юниты без соседа добавляются в результирующий список.
     * <p>
     * Алгоритмическая сложность: O(n),
     * где n - Общее количество юнитов в армии противника.
     * <p>
     * Подробный анализ сложности:
     * 1. Обход всех рядов: O(m), где m = 3 (фиксированное количество рядов)
     * 2. Формирование множества координат живых юнитов в ряду: O(k)
     * 3. Проверка наличия соседа через HashSet: O(1) для каждого юнита
     * 4. Итог для одного ряда: O(k) + O(k) = O(2k) = O(k)
     * <p>
     * Для всех рядов: O(k1 + k2 + k3) = O(n), где k1 + k2 + k3 = n
     * Итоговая сложность: O(n)
     * Реализация соответствует требованиям технического задания,
     * обеспечивает оптимальную производительность O(n) и корректно работает
     * при любом распределении юнитов на поле боя.
     */
    @Override
    public List<Unit> getSuitableUnits(List<List<Unit>> unitsByRow, boolean isLeftArmyTarget) {
        List<Unit> suitableUnits = new ArrayList<>();

        if (unitsByRow == null || unitsByRow.isEmpty()) {
            return suitableUnits;
        }

        for (List<Unit> row : unitsByRow) {
            if (row == null || row.isEmpty()) {
                continue;
            }

            Set<Integer> aliveYCoords = new HashSet<>();
            List<Unit> aliveUnits = new ArrayList<>();

            for (Unit unit : row) {
                if (unit != null && unit.isAlive()) {
                    aliveUnits.add(unit);
                    aliveYCoords.add(unit.getyCoordinate());
                }
            }

            for (Unit unit : aliveUnits) {
                int y = unit.getyCoordinate();

                boolean hasCoveringNeighbor = isLeftArmyTarget
                        ? aliveYCoords.contains(y - 1)
                        : aliveYCoords.contains(y + 1);

                if (!hasCoveringNeighbor) {
                    suitableUnits.add(unit);
                }
            }
        }

        return suitableUnits;
    }
}
