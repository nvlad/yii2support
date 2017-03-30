<?php

namespace app\controllers\test;

use app\models\Address;
use Yii;
use yii\data\ActiveDataProvider;
use yii\filters\AccessControl;
use yii\grid\GridView;
use yii\web\Application;
use yii\web\Controller;
use yii\filters\VerbFilter;
use app\models\LoginForm;
use app\models\ContactForm;

class SiteController extends Controller
{
    /**
     * @inheritdoc
     */
    public function behaviors()
    {
        return [
            'access' => [
                'class' => AccessControl::className(),
                'only' => ['logout'],
                'rules' => [
                    [
                        'actions' => ['logout'],
                        'allow' => true,
                        'roles' => ['@'],
                    ],
                ],
            ],
            'verbs' => [
                'class' => VerbFilter::className(),
                'actions' => [
                    'logout' => ['post'],
                ],
            ],
        ];
    }

    /**
     * @inheritdoc
     */
    public function actions()
    {
        return [
            'error' => [
                'class' => 'yii\web\ErrorAction',
            ],
            'captcha' => [
                'class' => 'yii\captcha\CaptchaAction',
                'fixedVerifyCode' => YII_ENV_TEST ? 'testme' : null,
            ],
        ];
    }

    /**
     * Displays homepage.
     *
     * @return string
     */
    public function actionIndex()
    {
        return $this->render('index');
    }

    /**
     * Login action.
     *
     * @return string
     */
    public function actionLogin()
    {
        if (!Yii::$app->user->isGuest) {
            return $this->goHome();
        }
        $test = new ActiveDataProvider([
            'sort' => [
                'urlManager' => [
                    'cache' => [
                        'keyPrefix' => 'fdf',
                        ''
                    ]
                ]
            ]
        ]);

        Yii::createObject(LoginForm::className(), [
            ''
        ]);
        $test = [
            'class' => LoginForm::className(),
            'rememberMe' => '',
            'scenario' => '',
            ''

        ];

        echo GridView::widget([
            'columns' => [
                ''
            ]
        ]);



        $model = new LoginForm();
        if ($model->load(Yii::$app->request->post()) && $model->login()) {
            return $this->goBack();
        }
        return $this->render('login', [
            'model' => $model,
        ]);
    }

    /**
     * Logout action.
     *
     * @return string
     */
    public function actionLogout()
    {
        Yii::$app->user->logout();

        return $this->goHome();
    }

    public function actionTest() {
        [
            'request' => [
                ''
            ]
        ];
    }

    /**
     * Displays contact page.
     *
     * @return string
     */
    public function actionContact()
    {
        $model = new ContactForm();
        if ($model->load(Yii::$app->request->post()) && $model->contact(Yii::$app->params['adminEmail'])) {
            Yii::$app->session->setFlash('contactFormSubmitted');

            return $this->refresh();
        }
        return $this->render('contact', [
            'model' => $model,
        ]);

    }

    /**
     * Displays about page.
     *
     * @return string
     */
    public function actionAbout()
    {
        $data = Address::find()->where(['or', ['or', [' => '1', 'firstname' => '2'], ['like', 'lastname', 'test']], ['tax_id' => 1]]);
        Address::find()->where(([""] ));
        return $this->render('about4', ['test' => $test]);
    }
}
