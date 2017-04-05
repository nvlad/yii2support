<?php

namespace yii\db {
    class Query{
        public function where($condition, $params = [])
        {

        }
    }

    class ActiveRecord {
        public static function find()
        {
            return new Query();

        }
    }
}

namespace test {

    use yii\db\ActiveRecord;

    /**
     * Class Person
     * @package test
     * @property $prop1
     * @property $prop2
     */
    class PersonModel extends ActiveRecord {
        public static function tableName()
        {
            return 'person';
        }
    }
}

