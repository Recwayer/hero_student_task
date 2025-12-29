package programs;

import com.battle.heroes.army.Army;
import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.GeneratePreset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GeneratePresetImpl implements GeneratePreset {
    /**
     * Формирует пресет армии компьютера с учётом заданных ограничений.
     * <p>
     * Метод создаёт армию, оптимизированную по соотношению атаки к стоимости,
     * а при равенстве — по соотношению здоровья к стоимости, не превышая
     * лимит очков и ограничение в 11 юнитов каждого типа.
     * <p>
     * Алгоритм:
     * Жадный алгоритм с предварительной сортировкой типов юнитов по показателям
     * эффективности и последующим добавлением максимального количества юнитов
     * каждого типа в порядке убывания эффективности.
     * <p>
     * Алгоритмическая сложность: O(n log n + m)
     * где:
     * n - общее число типов юнитов
     * m - максимальное число юнитов в армии
     * <p>
     * Подробный анализ сложности:
     * 1. Расчёт коэффициентов эффективности для каждого типа юнита: O(n)
     * 2. Сортировка типов юнитов по убыванию эффективности: O(n log n)
     * 3. Проход по отсортированным типам и добавление юнитов: O(n + m)
     *    - Внешний цикл по n типам: O(n)
     *    - Внутренний цикл создаёт суммарно m юнитов: O(m)
     * 4. Генерация позиций на поле: O(1) (константа 63 позиции)
     * 5. Перемешивание позиций: O(1) (константа)
     * 6. Размещение юнитов на позициях: O(m)
     * <p>
     * Итоговая сложность:
     * O(n) + O(n log n) + O(n + m) + O(1) + O(1) + O(m) = O(n log n + m)
     * <p>
     * Улучшение по сравнению с базовой сложностью O(n·m):
     * - Базовая сложность O(n*m): m итераций, на каждой поиск среди n типов
     * - Улучшенная сложность O(n log n + m): один проход по типам, создание всех юнитов
     * <p>
     * Реализация соответствует требованиям ТЗ и обеспечивает
     * эффективное формирование армии компьютера.
     */
    private static final int MAX_UNITS_PER_TYPE = 11;
    private static final int FIELD_WIDTH = 3;
    private static final int FIELD_HEIGHT = 21;

    @Override
    public Army generate(List<Unit> unitList, int maxPoints) {
        List<Unit> resultArmy = new ArrayList<>();

        List<UnitScore> scores = new ArrayList<>();
        for (Unit unit : unitList) {
            double attackRatio = (double) unit.getBaseAttack() / unit.getCost();
            double healthRatio = (double) unit.getHealth() / unit.getCost();
            scores.add(new UnitScore(unit, attackRatio, healthRatio));
        }

        scores.sort((a, b) -> {
            int attackCompare = Double.compare(b.attackEfficiency, a.attackEfficiency);
            if (attackCompare != 0) {
                return attackCompare;
            }
            return Double.compare(b.healthEfficiency, a.healthEfficiency);
        });

        int pointsLeft = maxPoints;

        for (UnitScore score : scores) {
            Unit template = score.unit;
            int canAdd = Math.min(MAX_UNITS_PER_TYPE, pointsLeft / template.getCost());
            int unitIndex = 1;
            for (int k = 0; k < canAdd; k++) {
                Unit newUnit = new Unit(
                        template.getUnitType() + " " + unitIndex++,
                        template.getUnitType(),
                        template.getHealth(),
                        template.getBaseAttack(),
                        template.getCost(),
                        template.getAttackType(),
                        template.getAttackBonuses(),
                        template.getDefenceBonuses(),
                        0,
                        0
                );
                newUnit.setAlive(true);
                newUnit.setProgram(template.getProgram());

                resultArmy.add(newUnit);
                pointsLeft -= template.getCost();
            }
        }


        List<Position> positions = generatePositions();
        Collections.shuffle(positions);

        for (int i = 0; i < resultArmy.size() && i < positions.size(); i++) {
            Position p = positions.get(i);
            resultArmy.get(i).setxCoordinate(p.x);
            resultArmy.get(i).setyCoordinate(p.y);
        }

        Army army = new Army(resultArmy);
        army.setPoints(maxPoints - pointsLeft);
        return army;
    }

    private List<Position> generatePositions() {
        List<Position> positions = new ArrayList<>();
        for (int y = 0; y < FIELD_HEIGHT; y++) {
            for (int x = 0; x < FIELD_WIDTH; x++) {
                positions.add(new Position(x, y));
            }
        }
        return positions;
    }

    private static class UnitScore {
        Unit unit;
        double attackEfficiency;
        double healthEfficiency;

        UnitScore(Unit unit, double attackEfficiency, double healthEfficiency) {
            this.unit = unit;
            this.attackEfficiency = attackEfficiency;
            this.healthEfficiency = healthEfficiency;
        }
    }

    private static class Position {
        int x;
        int y;

        Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
