import { RuleTester } from "eslint";

// @typescript-eslint/parser is required for the type assertion test at the end of this test file
const tsParserPath = require.resolve("@typescript-eslint/parser");
const ruleTester = new RuleTester({
  parserOptions: { ecmaVersion: 2018, sourceType: "module" },
  parser: tsParserPath,
});
import { rule } from "../../src/rules/no-redundant-parentheses";

ruleTester.run("Redundant pairs of parentheses should be removed", rule, {
  valid: [
    {
      code: `var a = typeof (38);`,
    },
    {
      code: `let a = typeof 39;`,
    },
    {
      code: `const a = (((a * b) + c) / 2.0);`,
    },
    {
      code: `if ((a = 3)) {}`,
    },
    {
      code: `while ((a = 3)) {}`,
    },
    {
      code: `do {} while ((a = 3))`,
    },
    {
      code: `let a = doSomething( /** @type MyObject */ (b));`,
    },
    {
      code: `let a = new MyClass((b = c));`,
    },
  ],
  invalid: [
    {
      code: `var a = typeof ((37));`,
      errors: [
        {
          message: `{"message":"Remove these useless parentheses.","secondaryLocations":[{"column":20,"line":1,"endColumn":21,"endLine":1}]}`,
          line: 1,
          endLine: 1,
          column: 16,
          endColumn: 17,
        },
      ],
    },
    {
      code: `const a = ((((a * b) + c)) / 2.0);`,
      errors: [
        {
          message:
            '{"message":"Remove these useless parentheses.","secondaryLocations":[{"column":25,"line":1,"endColumn":26,"endLine":1}]}',
          line: 1,
          endLine: 1,
          column: 12,
          endColumn: 13,
        },
      ],
    },
    {
      code: `
        (
         (
          (a)
             )
              )`,
      errors: [
        {
          message:
            '{"message":"Remove these useless parentheses.","secondaryLocations":[{"column":14,"line":6,"endColumn":15,"endLine":6}]}',
          line: 2,
          endLine: 2,
          column: 9,
          endColumn: 10,
        },
        {
          message:
            '{"message":"Remove these useless parentheses.","secondaryLocations":[{"column":13,"line":5,"endColumn":14,"endLine":5}]}',
          line: 3,
          endLine: 3,
          column: 10,
          endColumn: 11,
        },
      ],
    },
    {
      // Type assertion test requiring @typescript-eslint/parser
      code: `if (myBool) { ((<myCast>obj)).methodCall() }`,
      errors: [
        {
          message:
            '{"message":"Remove these useless parentheses.","secondaryLocations":[{"column":28,"line":1,"endColumn":29,"endLine":1}]}',
          line: 1,
          endLine: 1,
          column: 15,
          endColumn: 16,
        },
      ],
    },
    {
      code: `(((((a)))))`,
      errors: 4,
    },
    {
      code: `let a = doSomething(( /** @type MyObject */ (b)));`,
      errors: 1,
    },
    {
      code: `if (((a = 3))) {}`,
      errors: 1,
    },
    {
      code: `while (((a = 3))) { ((a = 5)); }`,
      errors: 2,
    },
    {
      code: `let a = new MyClass(((b = c)));`,
      errors: 1,
    },
  ],
});
