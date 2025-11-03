# Assignment 4 Smart City
## How to run and build
### Build
```bash
mvn clean compile
```

### Run Tests
```bash
mvn test
```

### Generate Datasets
```bash
mvn exec:java -Dexec.mainClass="com.smartcity.util.DatasetGenerator"
```

### Run Main Analysis
```bash
# Analyze the provided tasks.json
mvn exec:java -Dexec.mainClass="com.smartcity.Main" -Dexec.args="data/tasks.json"

# Analyze other datasets
mvn exec:java -Dexec.mainClass="com.smartcity.Main" -Dexec.args="data/medium_mixed_1.json"
```

### Run Benchmark Analysis
```bash
mvn exec:java -Dexec.mainClass="com.smartcity.benchmark.BenchmarkRunner"

```
## Data Summary
### Small Datasets (6-10 nodes)
1. **small_dag_1.json**: 6 nodes, pure DAG, sparse (30% density)
2. **small_cycle_1.json**: 8 nodes, contains cycles, medium density (40%)
3. **small_dag_2.json**: 10 nodes, pure DAG, sparse (25% density)

### Medium Datasets (10-20 nodes)
4. **medium_mixed_1.json**: 12 nodes, multiple SCCs, medium density (30%)
5. **medium_mixed_2.json**: 15 nodes, multiple SCCs, medium density (35%)
6. **medium_dag_1.json**: 18 nodes, pure DAG, sparse (20% density)

### Large Datasets (20-50 nodes)
7. **large_sparse_1.json**: 25 nodes, contains SCCs, sparse (15% density)
8. **large_mixed_1.json**: 35 nodes, multiple SCCs, medium density (25%)
9. **large_dag_1.json**: 50 nodes, pure DAG, very sparse (10% density)

### Weight Model
All datasets use **edge weights** (stored in the `w` field of each edge). Weights range from 1 to 10.

## Result
| Dataset              | Nodes | Edges | Density  | Has Cycles | SCC Time (ns) | SCC DFS Visits | SCC Edges Explored | SCC Stack Pops | Num SCCs | Topo Time (ns) | Topo Queue Pushes | Topo Queue Pops | Topo Edges Processed | Topo Success | DAGSP Time (ns) | DAGSP Relaxations | DAGSP Updates | Critical Path Length |
|----------------------|--------|--------|-----------|-------------|----------------|-----------------|--------------------|----------------|-----------|-----------------|--------------------|-----------------|----------------------|---------------|-----------------|-------------------|----------------|-----------------------|
| large_dag_1.json     | 50     | 108    | 0.0441   | false       | 46600          | 50              | 108                | 50             | 50        | 23600           | 50                 | 50              | 108                  | true          | 11800           | 4                 | 4              | 8                     |
| large_mixed_1.json   | 35     | 297    | 0.2496   | true        | 45600          | 35              | 297                | 35             | 1         | 1500            | 1                  | 1               | 0                    | true          | 1500            | 0                 | 0              | 0                     |
| large_sparse_1.json  | 25     | 90     | 0.1500   | true        | 23500          | 25              | 90                 | 25             | 1         | 1100            | 1                  | 1               | 0                    | true          | 3900            | 0                 | 0              | 0                     |
| medium_dag_1.json    | 18     | 29     | 0.0948   | false       | 20000          | 18              | 29                 | 18             | 18        | 9500            | 18                 | 18              | 29                   | true          | 10000           | 11                | 9              | 17                    |
| medium_mixed_1.json  | 12     | 39     | 0.2955   | true        | 16300          | 12              | 39                 | 12             | 1         | 1300            | 1                  | 1               | 0                    | true          | 1400            | 0                 | 0              | 0                     |
| medium_mixed_2.json  | 15     | 73     | 0.3476   | true        | 19800          | 15              | 73                 | 15             | 1         | 1900            | 1                  | 1               | 0                    | true          | 1500            | 0                 | 0              | 0                     |
| small_cycle_1.json   | 8      | 22     | 0.3929   | true        | 9800           | 8               | 22                 | 8              | 1         | 1600            | 1                  | 1               | 0                    | true          | 1300            | 0                 | 0              | 0                     |
| small_dag_1.json     | 6      | 4      | 0.1333   | false       | 9500           | 6               | 4                  | 6              | 6         | 3700            | 6                  | 6               | 4                    | true          | 4400            | 2                 | 2              | 10                    |
| small_dag_2.json     | 10     | 14     | 0.1556   | false       | 11600          | 10              | 14                 | 10             | 10        | 4700            | 10                 | 10              | 14                   | true          | 29700           | 14                | 14             | 50                    |
| tasks.json           | 8      | 7      | 0.1250   | true        | 9500           | 8               | 7                  | 8              | 6         | 2800            | 6                  | 6               | 4                    | true          | 4900            | 3                 | 3              | 8                     |

## Algorithm Analysis

### SCC Detection (Tarjan’s Algorithm)

Tarjan’s algorithm identifies **Strongly Connected Components (SCCs)** in a directed graph using a single depth-first search traversal. It tracks how many vertices are visited, how many edges are explored, and how often elements are pushed or popped from the internal stack. The number of SCCs discovered is also recorded to understand the graph’s cyclic structure.

This algorithm runs in **linear time O(V + E)**, as each vertex and edge is processed only once. It uses additional memory proportional to the number of vertices for arrays and the recursion stack. Because of its efficient single-pass nature, Tarjan’s algorithm performs particularly well on **dense graphs with cycles**, where it can quickly isolate tightly connected groups of nodes.

---

### Topological Sort (Kahn’s Algorithm)

Topological sorting is carried out using **Kahn’s Algorithm**, which applies a breadth-first approach to order nodes in a **directed acyclic graph (DAG)**. During the run, the algorithm counts how many elements are pushed into and removed from the queue and how many edges are processed. It maintains an in-degree array to determine which nodes have no incoming edges at each step.

The algorithm executes in **O(V + E)** time and requires memory proportional to the number of vertices. Its main strength lies in detecting cycles and constructing a valid dependency order when the graph is acyclic. This makes it highly suitable for **dependency resolution and scheduling problems**.

---

### DAG Shortest/Longest Paths

Once a valid topological order is available, the shortest or longest paths in a **DAG**
can be computed efficiently by relaxing each edge in that order. 
During this phase, the algorithm records how many relaxations and distance updates occur, 
as well as the length of the final critical path.

This process also operates in **O(V + E)** time and uses memory proportional 
to the number of vertices to store distance and predecessor information. 
Compared to more general algorithms like Dijkstra’s, this approach is **much faster** 
for DAGs since it avoids the need for a priority queue. It is particularly effective 
for **project scheduling and critical path analysis**, where tasks must be completed in sequence.

---

### Bottlenecks and Performance Factors

Across all algorithms, performance is influenced by both **graph density** and **the structure of SCCs**.

For **SCC detection**, dense graphs with numerous interconnections tend to 
increase the number of edges explored but do not drastically affect runtime, 
since Tarjan’s algorithm remains linear. However, large or deeply nested SCCs 
may lead to higher stack usage and more complex recursion paths.

In **topological sorting**, bottlenecks typically arise when many nodes 
share similar in-degrees, causing multiple queue operations at once. 
Sparse DAGs perform best, while graphs with high connectivity or residual 
cycles can slow down processing due to repeated checks for acyclicity.

For **DAG shortest/longest path computations**, the graph’s density and 
topology also play a significant role. Sparse DAGs with long linear chains 
result in straightforward relaxation sequences, whereas denser graphs require 
more relaxation operations, though still maintaining linear time. 
The **critical path length** increases with graph complexity, directly 
affecting total computation time.

Overall, while all three algorithms scale linearly, their actual 
performance varies depending on how **graph density** and **SCC structure** 
interact, making these two factors key determinants of efficiency in practical scenarios.

### Effect of Graph Density and SCC Structure on Performance

The **density** and **SCC structure** of a graph have a direct and sometimes subtle impact on the performance of algorithms like Tarjan’s SCC detection, Kahn’s topological sort, and DAG shortest/longest path computations. Although these algorithms all have theoretical linear complexity — **O(V + E)** — their actual runtime behavior can vary greatly based on how the graph is organized.

---

#### 1. Graph Density

**Graph density** measures how many edges exist relative to the maximum possible number of edges. Sparse graphs have relatively few edges, while dense graphs have many interconnections between nodes.

In **sparse graphs**, algorithms tend to run faster in practice because there are fewer edges to explore, process, or relax. Each traversal step is simpler, queue operations are lighter, and the total number of adjacency lookups remains small. Sparse structures also make topological sorting easier, as the number of in-degree updates and edge relaxations stays minimal.

In contrast, **dense graphs** — where most nodes are connected to many others — cause algorithms to perform significantly more edge-related operations. Although the asymptotic complexity remains linear, the constant factors grow. For Tarjan’s SCC algorithm, dense graphs increase the number of recursive edge explorations, stack manipulations, and backtracking steps. For Kahn’s algorithm and DAG path calculations, higher edge counts mean more in-degree adjustments, queue operations, and relaxation checks. As a result, even though runtime still scales linearly, dense graphs demand more CPU cycles, memory access, and cache bandwidth.

---

#### 2. SCC Structure

The **structure and size of SCCs** also strongly influence runtime. In graphs with many small, simple SCCs, Tarjan’s algorithm quickly identifies and isolates each component with minimal stack usage. Each recursive call terminates early, leading to predictable and low memory consumption.

However, when SCCs are **large or deeply nested**, the recursion depth and stack utilization grow significantly. Tarjan’s algorithm must traverse deeper call chains and maintain more active elements in its internal data structures, increasing both execution time and memory pressure. In extreme cases, highly cyclic graphs — where nearly all nodes are mutually reachable — produce a single massive SCC. This causes the algorithm to perform more internal comparisons, repeated edge checks, and delayed stack pops, extending overall runtime.

For **topological sorting**, SCC structure plays an indirect but critical role. Acyclic graphs (DAGs) run smoothly through Kahn’s algorithm, but the presence of SCCs (cycles) can completely prevent a valid ordering. If SCCs are not identified and removed beforehand, Kahn’s algorithm can become stuck waiting for zero in-degree vertices that never appear. Therefore, the existence and arrangement of SCCs determine whether topological sorting is even feasible.

Similarly, in **DAG shortest or longest path computations**, SCC structure affects the preprocessing step. Before path relaxation can begin, cycles must be collapsed or removed. Larger SCCs delay this step and can cause path computations to become invalid unless the graph is properly reduced to a DAG.

## Conclusion

The comparative analysis of Tarjan’s SCC detection, Kahn’s topological sort, and DAG shortest/longest path algorithms demonstrates that while all three operate with linear theoretical complexity, their practical performance and applicability depend strongly on the **graph’s structure**, particularly **density** and **SCC composition**. Sparse, well-structured DAGs consistently yield optimal results with minimal overhead, while dense graphs and large cyclic components increase recursion depth, memory use, and queue operations.

From a practical standpoint, each algorithm serves a specific role within the broader process of directed graph analysis. **Tarjan’s algorithm** is the best starting point when cycles may exist, efficiently grouping strongly connected vertices and revealing the graph’s internal structure. Once cycles are resolved, **Kahn’s topological sort** provides a clear, dependency-respecting order of execution, suitable for task scheduling and dependency resolution. Finally, the **DAG shortest or longest path algorithm** builds upon that ordering to evaluate timing, cost, or sequence length — making it ideal for applications such as project scheduling and critical path analysis.

In real-world workflows, these algorithms are most effective when used **together in sequence**: Tarjan’s method identifies SCCs and converts the graph into a DAG; Kahn’s algorithm then determines the execution order; and the DAG path algorithm computes the optimal or critical sequence. This pipeline ensures both correctness and efficiency, allowing complex systems — from build pipelines to network models — to be analyzed and optimized systematically.

Ultimately, the choice between these algorithms depends on **what the problem demands**:
- Use **Tarjan’s** for detecting and collapsing cycles.
- Use **Kahn’s** for ordering dependencies in acyclic graphs.
- Use **DAG-SP** for computing optimal or critical paths once the graph is acyclic.

Understanding how graph density and SCC structure influence each stage allows for more efficient design, better memory management, and predictable runtime behavior in practical implementations.
