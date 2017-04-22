IntelliJ IDEA / PhpStorm Yii2 Support
====================================

[![Latest Stable Version](http://phpstorm.espend.de/badge/9388/version)](https://plugins.jetbrains.com/plugin/9388)
[![Total Downloads](http://phpstorm.espend.de/badge/9388/downloads)](https://plugins.jetbrains.com/plugin/9388)
[![Downloads Last Month](http://phpstorm.espend.de/badge/9388/last-month)](https://plugins.jetbrains.com/plugin/9388)

<p align="center">
    <a href="https://plugins.jetbrains.com/idea/plugin/9388-yii2-support" target="_blank">
        <img src="img/logo.png?raw=true" width="518" alt="IntelliJ IDEA / PhpStorm Yii2 Support" />
    </a>
</p>


Provides [Yii 2 Framework](http://www.yiiframework.com/) support for [PhpStorm](https://www.jetbrains.com/phpstorm/)
and [IntelliJ IDEA](https://www.jetbrains.com/idea/).

Features
--------

### Views

- View template names and parameters completion
- Add view parameters after completion
- Inspection for missing view templates
- QuickFix for missing view templates
- Jump to View file (go to declaration)
- Inspection for required and unused template parameters
- QuickFix for required and unused template parameters
- Updates path to view template when file is moved

### i18n

- Code completion
- Generate params array

### Configuration arrays

Code completion for Yii configuration arrays. Works both in configuration files and on object instantiation.
Following cases are supported:

- Array in `$config` parameter in `yii\base\Object` or its descendants constructor
- Array has a `class` key with valid class representation: fully qualified name as string, `ClassName::class` or `Class::className()`
- Array is a value of a key that corresponds to standard Yii classes (like `db`, `request`, `mailer`, and so on), and
  a file with this array is located within `config` directory
- `WidgetClass::widget()` and `WidgetClass::begin` calls in case `WidgetClass` is a descendant of `yii\base\Widget`
- `$field->widget()` method call on `yii\widgets\ActiveField` and its descendants
- Inside array in `GridView`, `columns` key
- `Yii::createObject` method

Go To Declaration, Rename, Find usages and Help popups work whenever code completion works.

### Database support

**Database connection is required**

#### ActiveQuery
- Code completion for `ActiveQuery` (`ActiveRecord::find()->where` for example)  
- Code completion inside `ActiveQuery` linked to ActiveRecord  
- Inspection in case if `ActiveQuery` not linked to `ActiveRecord`
#### ActiveRecord
- Code completion for `ActiveRecord` `findAll()`, `findOne()` and so on methods  
- Undetectable `ActiveRecord` table inspection  
- Code completion in relations methods  
#### Migrations
- Code completion for migrations  
#### Condition parameters
- Condition parameters code completion  
- Condition parameters inspection  
#### Properties
- Synchronize properties with database  
- Unused properties inspection  

Installation
------------

- Open your PhpStorm or IntelliJ IDEA IDE.
- Go to `File` → `Settings`.
- Choose `Plugins`.
- Press `Browse repositories...` button.
- Type `yii2 support`.
- Press `Install` green button on the very top of description.

Contributing
------------

The plugin is [Open Source](LICENSE.md). You may contribute either by testing and [reporting issues](https://github.com/nvlad/yii2support/issues)
or by sending pull requests. 

### Spreading the Word

Acknowledging and or citing the plugin is as important as direct contributions.

If you are giving a presentation or talk we suggest using
[our logo](https://github.com/nvlad/yii2support/raw/master/img/logo.png?raw=true).

