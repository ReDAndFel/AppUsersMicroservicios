<?php

namespace App\Services;

use Illuminate\Support\Facades\Log;
use PhpAmqpLib\Connection\AMQPStreamConnection;
use PhpAmqpLib\Message\AMQPMessage;

class RabbitMQConsumer
{
    protected $connection;
    protected $channel;

    public function __construct()
    {
        try {
            $this->connection = new AMQPStreamConnection(
                config('services.rabbitmq.host'),
                config('services.rabbitmq.port'),
                config('services.rabbitmq.user'),
                config('services.rabbitmq.password'),
                config('services.rabbitmq.vhost')
            );
            $this->channel = $this->connection->channel();
        } catch (\Throwable $th) {
            return response()->json(['error' => 'Error connecting to RabbitMQ'], 500);
        }
        
    }

    public function consume(callable $callback)
    {
        $queue = 'services.rabbitmq.queue';
        $this->channel->queue_declare($queue, false, true, false, false);

        $this->channel->basic_consume($queue, '', false, true, false, false, function (AMQPMessage $message) use ($callback) {
            $callback($message->getBody());
            $message->ack();
        });      

        while ($this->channel->is_consuming()) {
            $this->channel->wait();
        }

        $this->channel->close();
        $this->connection->close();
    }
}