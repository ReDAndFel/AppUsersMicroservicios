<?php

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;
use App\Http\Controllers\LogController;

Route::get('/logs', [LogController::class, 'getLogs']);

Route::post('/logs',[LogController::class, 'insertLog']);

Route::get('/logs/{application}',[LogController::class, 'getLogsByApplication']);