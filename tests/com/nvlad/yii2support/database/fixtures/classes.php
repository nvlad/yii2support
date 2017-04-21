<?php

namespace yii\db {
    class Query{
        public function where($condition, $params = [])
        {

        }


    }

    class BaseActiveRecord {

        /**
         * @return Query
         */
        public static function find()
        {
            return new Query();

        }
    }

    class ActiveRecord extends BaseActiveRecord {

    }
}

namespace test {

    use yii\db\ActiveRecord;

    /**
     * Class Person
     * @package test
     * @property $prop1
     * @property $prop2
     * @property $test
     */
    class PersonModel extends ActiveRecord {
        public static function tableName()
        {
            return 'person';
        }
    }

    class AddressModel extends ActiveRecord {
        public static function tableName()
        {
            return 'address';
        }
    }
}

