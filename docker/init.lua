box.cfg({listen = 3301})

local space = box.space.KV
if space == nil then
    space = box.schema.space.create('KV', {
        if_not_exists = true,
        engine = 'memtx'
    })

    space:format({
        {name = 'key', type = 'string'},
        {name = 'value', type = 'varbinary', is_nullable = true}
    })

    space:create_index('primary', {
        type = 'TREE',
        parts = {'key'},
        unique = true,
        if_not_exists = true
    })

    print('Спейс KV создан')
end

print('Tarantool доступен на порту 3301')