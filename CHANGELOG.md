Yii2 Support Change Log
=======================

0.7.35.2 December 27, 2017
--------------------------
- Form's field method autocomplete

0.7.35.1 December 12, 2017
--------------------------
- Url autocomplete 

0.6.34.19 December 1, 2017
--------------------------
- Object class deprecated

0.6.34.18 November 9, 2017
--------------------------
- PHPStorm 2017.3 Support
- PostgreSQL support

0.6.34.16 October 10, 2017
--------------------------
- Fix table prefix support
- Fix detection unused variables in Views with Unary or SelfAssignment operators
- Disabling MissedViewInspection for Controllers & Widgets with overwritten getViewPath method

0.6.34.13 September 28, 2017
----------------------------
- Table prefix support using plugin settings

0.5.33.12 July 24, 2017
-----------------------
- ActiveRecord type provider re-enabled (Issue #129)
- Track variables and class fields to provide autocomplete

0.5.33.11 July 12, 2017
-----------------------
- ActiveRecord type provider was disabled due to performance issues (Issue #129).
- TypeProvider for Yii::createObject disabled for case when parameter is variable (Issue #129).

0.5.33.10 July 12, 2017
-----------------------
- ActiveRecord type provider was disabled due limitations of access to index (Issue #129).

0.5.33.8 July 30, 2017
----------------------
- Issues #115, #126. Now inspections does not work for classes in root namespace to avoid collision with standard classes.

0.5.33.7 June 30, 2017
----------------------
- Autocomplete in model's rules method
- Calculates return type for Yii::createObject call and one/all method call of model

0.4.30.4 April 27, 2017
-----------------------
- Better detection of condition parameters
- Exception fix

0.4.30.3 April 24, 2017
-----------------------
- Complete attributes for $form->field($model, ...) & Html::active*($model, ...) methods
- Completion for setters $activeDataProvider->setSort([...])
- Fix minor bugs

0.4.28.1 April 22, 2017
-----------------------
- Database support (database connection required)
- Fix false-positive inspections for non Yii2 render() methods

0.3.17.9 April 5, 2017
----------------------
- False-positive missed field inspection for \Closure class - Small fixes

0.3.17.5 March 29, 2017
-----------------------
- Ignore method and function(except "compact") calls as second parameter for render* functions
- False-positive missed field inspection on event & behavior declaration in config object create array
- Fix exception from UnusedParametersLocalQuickFix in older PhpStorm versions

0.3.17.2 March 24, 2017
-----------------------
- Fix exception with resolved non class on get class by instantiation
- Fix not worked RequireParameterQuickFix on empty second parameter

0.3.17.0 March 22, 2017
-----------------------
- Code completion for object configuration array (with "class" key), config files, widgets and object creating

0.2.10.11 February 8, 2017
--------------------------
- Fix variables used in closures mark as unused
- Fix parameter declared with name identical variable in function scope mark as unused

0.2.10.9 February 3, 2017
-------------------------
- Update path to View file on file move
- Fixed replace View path on file rename
- Fix mark as error view paths started with "//" or "@" (references to files is not work)

0.2.9.7 February 1, 2017
------------------------
- Refactor work with Views
- Support View in Widgets
- Split missed view file & missed parameters inspections
- Fixed false-positive .twig & .tpl View files as missed
- Fixed adding SUPERGLOBAL variables in render parameters
- Fixed variables in strings return empty name
- Fix using ".." in View path

0.2.7.2 January 19, 2017
------------------------
- Fix Exception on false-positive references for function call inline comments
- Missed & unused parameters inspections work with `compact` function

0.2.7 January 18, 2017
----------------------
- Add View parameters after completion
- Inspection missed View files
- QuickFix for missed files
- Inspection by required & unused parameters for View render
- QuickFix for required & unused parameters

0.2.2 January 12, 2017
----------------------
- Fix show autocompletion popup

0.2.1 January 10, 2017
----------------------
- Change i18n message view in completion list
- Resolve composite key & values for i18n

0.2 January 8, 2017
-------------------
- Add i18n support
