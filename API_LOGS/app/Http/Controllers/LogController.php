<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;
use App\Models\Log;

class LogController extends Controller
{
    public function getLogs(Request $request)
    {
        $query = Log::query();

        // Filtrar por rango de fechas
        if ($request->has('from_date') && $request->has('to_date')) {
            $query->whereBetween('created_at', [$request->from_date, $request->to_date]);
        }

        // Filtrar por tipo de log
        if ($request->has('tipo')) {
            $query->where('tipo', $request->tipo);
        }

        // Ordenar por fecha de creación
        $query->orderBy('created_at', 'desc');

        // Aplicar paginación
        $logs = $query->paginate(15); // Puedes ajustar el número de registros por página

        return response()->json($logs, 400);
    }

    public function insertLog(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'tipo' => 'required',
            'aplicacion' => 'required',
            'clase_modulo' => 'required',
            'resumen' => 'required',
            'descripcion' => 'required'
        ]);

        if ($validator->fails()) {
            $data = [
                'message' => 'Error en la validacion de los datos',
                'errors' => $validator->errors(),
                'status' => 400
            ];
            return response()->json($data, 400);
        }

        $log = Log::create([
            'tipo' => $request->tipo,
            'aplicacion' => $request->aplicacion,
            'clase_modulo' => $request->clase_modulo,
            'resumen' => $request->resumen,
            'descripcion' => $request->descripcion
        ]);

        if (!$log) {
            $data = [
                'message' => 'Error al crear el log',
                'status' => 500
            ];
            return response()->json($data, 500);
        }

        $data = [
            'log' => $log,
            'status' => 201
        ];

        return response()->json($data, 201);
    }

    public function getLogsByApplication(Request $request, $application)
    {
        $query = Log::where('aplicacion', $application);

        // Filtrar por rango de fechas
        if ($request->has('from_date') && $request->has('to_date')) {
            $query->whereBetween('created_at', [$request->from_date, $request->to_date]);
        }

        // Filtrar por tipo de log
        if ($request->has('tipo')) {
            $query->where('tipo', $request->tipo);
        }

        // Ordenar por fecha de creación
        $query->orderBy('created_at', 'desc');

        // Aplicar paginación
        $logs = $query->paginate(15); // Puedes ajustar el número de registros por página

        return response()->json($logs,200);
    }
}
