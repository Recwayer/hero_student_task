package programs;

import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.Edge;
import com.battle.heroes.army.programs.UnitTargetPathFinder;

import java.util.*;

public class UnitTargetPathFinderImpl implements UnitTargetPathFinder {
    private static final int WIDTH = 27;
    private static final int HEIGHT = 21;

    private static final int[][] DIRECTIONS = {
            {-1, -1}, {-1, 0}, {-1, 1},
            {0, -1}, {0, 1},
            {1, -1}, {1, 0}, {1, 1}
    };

    /**
     * Определяет кратчайший маршрут между атакующим и атакуемым юнитом.
     * <p>
     * Метод находит кратчайший путь на игровом поле с учётом препятствий
     * в виде других живых юнитов. Движение разрешено в 8 направлениях
     * (горизонтально, вертикально и по диагоналям).
     * <p>
     * Алгоритм:
     * Реализация алгоритма A* (A-star) для поиска пути на сетке.
     * <p>
     * Основные шаги алгоритма:
     * 1. Инициализация начального узла с координатами атакующего юнита
     * 2. Построение множества заблокированных клеток из списка препятствий
     * 3. Итеративная обработка узлов из приоритетной очереди (открытого множества)
     * 4. Для каждого узла проверка всех 8 соседних клеток
     * 5. Восстановление пути при достижении цели
     * <p>
     * Алгоритмическая сложность: O(V * log V),
     * где V — максимальное количество посещаемых вершин (клеток поля).
     * <p>
     * Подробный анализ сложности:
     * 1. Построение множества заблокированных клеток: O(n), где n — количество юнитов-препятствий
     * 2. Инициализация структур данных: O(1)
     * 3. Основной цикл A* в худшем случае посещает все клетки поля: O(W * H),
     * где W - ширина поля, H - высота поля
     * 4. Каждая операция с приоритетной очередью (вставка/извлечение): O(log V)
     * 5. Проверка 8 соседей для каждой клетки: O(1)
     * <p>
     * Количество вершин V в худшем случае: V = W * H
     * <p>
     * Сложность одной итерации основного цикла:
     * O(log V) + O(1) = O(log V)
     * <p>
     * Итоговая сложность:
     * O(V) * O(log V) = O(V * log V) = O((W * H) * log(W * H))
     * <p>
     * Реализация соответствует требованиям технического задания,
     * обеспечивает оптимальный поиск кратчайшего пути и устойчиво работает
     * при различных конфигурациях препятствий на поле.
     */
    @Override
    public List<Edge> getTargetPath(Unit attackUnit, Unit targetUnit, List<Unit> obstacles) {

        if (attackUnit == null || targetUnit == null || obstacles == null) {
            return Collections.emptyList();
        }

        int startX = attackUnit.getxCoordinate();
        int startY = attackUnit.getyCoordinate();
        int targetX = targetUnit.getxCoordinate();
        int targetY = targetUnit.getyCoordinate();

        if (startX < 0 || startY < 0 || targetX < 0 || targetY < 0) {
            return Collections.emptyList();
        }

        if (startX == targetX && startY == targetY) {
            return List.of(new Edge(startX, startY));
        }

        Set<String> blocked = new HashSet<>();
        for (Unit u : obstacles) {
            if (u != null && u.isAlive() && !(u.getxCoordinate() == targetX && u.getyCoordinate() == targetY)) {
                blocked.add(u.getxCoordinate() + "," + u.getyCoordinate());
            }
        }

        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingInt(n -> n.f));
        Map<String, Node> all = new HashMap<>();

        Node start = new Node(startX, startY, null, 0,
                heuristic(startX, startY, targetX, targetY));

        open.add(start);
        all.put(start.key(), start);

        while (!open.isEmpty()) {
            Node current = open.poll();

            if (current.x == targetX && current.y == targetY) {
                return buildPath(current);
            }

            for (int[] d : DIRECTIONS) {
                int nx = current.x + d[0];
                int ny = current.y + d[1];

                if (!isValid(nx, ny)) continue;
                if (blocked.contains(nx + "," + ny)) continue;

                int g = current.g + 1;
                String key = nx + "," + ny;

                Node next = all.get(key);
                if (next == null || g < next.g) {
                    int h = heuristic(nx, ny, targetX, targetY);
                    Node node = new Node(nx, ny, current, g, g + h);
                    all.put(key, node);
                    open.add(node);
                }
            }
        }

        return Collections.emptyList();
    }

    private int heuristic(int x, int y, int tx, int ty) {
        return Math.max(Math.abs(x - tx), Math.abs(y - ty));
    }

    private List<Edge> buildPath(Node node) {
        LinkedList<Edge> path = new LinkedList<>();
        while (node != null) {
            path.addFirst(new Edge(node.x, node.y));
            node = node.parent;
        }
        return path;
    }

    private static class Node {
        int x, y;
        int g;
        int f;
        Node parent;

        Node(int x, int y, Node parent, int g, int f) {
            this.x = x;
            this.y = y;
            this.parent = parent;
            this.g = g;
            this.f = f;
        }

        String key() {
            return x + "," + y;
        }
    }

    private boolean isValid(int x, int y) {
        return x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT;
    }
}
