const regs = {
    email: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,
    password: /^[a-zA-Z0-9@$!%*?&]{8,18}$/
}

const verify = (rule, value, reg, callback) => {
    if (value) {
        if (reg.test(value)) {
            callback();
        }
        else {
            callback(new Error(rule.message));
        }
    }
    else {
        callback();
    }
}

export default {
    email: (reule, value, callback) => {
        return verify(reule, value, regs.email, callback);
    },
    password: (reule, value, callback) => {
        return verify(reule, value, regs.password, callback);
    }
}