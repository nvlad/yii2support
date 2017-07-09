<?php

namespace  yii\base {

    class Object
    {
        function __construct($config)
        {
        }
    }

    class Widget extends Object
    {
        public static function widget($config)
        {

        }

        public static function begin($config)
        {

        }
    }

    /**
     * Class TestWidget
     * @property $test1
     * @package yii\base
     */
    class TestWidget extends Widget
    {
        public $test2;
    }
}

namespace  yii\web {

    use yii\base\Object;

    class Request extends Object {
        /**
         * @var SubObject|string
         */
        var $subobject;
    }

    /**
     * Class SubObject
     * @property $test1 string
     * @package yii\web\Request
     */
    class SubObject extends Object{
        var $test2;
        function setTest3($value) {

        }
    }
}

namespace yii {
    class BaseYii {
        public static function createObject($type, array $params = []) {

        }
    }

    class Yii extends BaseYii {

    }
}

namespace yii\grid {

    use yii\base\Object;

    class GridView extends Object {
        public $column;

        public function setColumn(DataColumn $column)
        {

        }
    }

    class DataColumn extends Object{
        public $test1;
        public $test2;
    }
}

