<?php

namespace yii\helpers {
    class BaseUrl
    {
        public static function to($url = '', $scheme = false)
        {
        }

        public static function remember($url = '', $name = null)
        {
        }
    }

    class Url extends BaseUrl
    {
    }
}

namespace yii\web {
    class Controller
    {
        public function redirect($url)
        {
        }
    }
}

namespace app\controllers {
    class HomeController extends \yii\web\Controller
    {
        public function actionIndex()
        {
        }

        public function actionAbout()
        {
        }

        public function actionTransactions()
        {
        }

        public function actionCarController($id, $action)
        {
        }
    }

    class RoomControllerController extends \yii\web\Controller
    {
        public function actionIndex()
        {
        }

        public function actionTransactions()
        {
        }

        public function actionTvController()
        {
        }
    }
}