<?php

namespace yii\base {

    class BaseObject
    {
        function __construct($config)
        {
        }
    }

    class Model
    {
        function __construct($config)
        {
        }
    }
}

namespace yii\validators {
    class Validator
    {
    }

    class TestValidator extends Validator
    {
        public $param1;
        public $param2;
        public $param3;
        public $param4;
        public $param5;
    }
}

namespace app {
    use yii\base\Model;

    class ContactForm extends Model
    {
        public $name;
        public $email;
        public function rules()
        {
            return [
                [
                    ['name', 'email', 'subject', 'body'], 'required'],
                    ['<caret>']
                ];
        }
    }
}
