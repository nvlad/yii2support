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

namespace  yii\web\Request {

    use yii\base\Object;

    class Request extends Object {

    }
}

