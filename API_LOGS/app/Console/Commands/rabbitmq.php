<?php

namespace App\Console\Commands;

use Illuminate\Console\Command;
use App\Services\RabbitMQConsumer;

class rabbitmq extends Command
{
    protected $signature = 'rabbitmq:receive';
    protected $description = 'Receive messages from RabbitMQ';
    protected $rabbitMQConsumer;

    public function __construct(RabbitMQConsumer $rabbitMQConsumer)
    {
        parent::__construct();
        $this->rabbitMQConsumer = $rabbitMQConsumer;
    }

    public function handle()
    {
        $this->rabbitMQConsumer->consume(function ($message) {
            // Procesa el mensaje recibido
            $this->processMessage($message);
        });
    }

    protected function processMessage($message)
    {
        // Aquí puedes procesar el mensaje recibido
        $this->info('Received message: ' . $message);
    }
}
