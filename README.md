# КПЗ-лаб 1 (back-end)
## Виконали
Студенти групи ІПЗ-34мс

Кушнір Анастасія

Галайко Віталій

-------------
### End-points
1. POST /kpz/schema
  Input:
  ```json
  {
   "lang": "java",
   "schema": "start\ninput a, b\nv sum = a + b\nprint sum\nend"
  }
  ```
  Response:
  - 200 with file
  - 400 with error
2. POST /kpz/tests
  Input:
  ```json
  {
   "schemaRequest": {
     "lang": "java",
     "schema": "start\ninput a, b\nv sum = a + b\nprint sum\nend"
   },
   "testCases": [
     {
       "input": [1, 2],
       "expected": 3
     },
     {
       "input": [1, 2],
       "expected": 5
     }
   ]
  }
  ```
  Response:
  - 200 with tests result in str
  - 400 with error
